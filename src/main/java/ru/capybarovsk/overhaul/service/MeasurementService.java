package ru.capybarovsk.overhaul.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.capybarovsk.overhaul.dao.InvoiceDao;
import ru.capybarovsk.overhaul.dao.MeasurementDao;
import ru.capybarovsk.overhaul.model.Measurement;
import ru.capybarovsk.overhaul.model.User;

@Service
public class MeasurementService {
    private static final Logger logger = LoggerFactory.getLogger(MeasurementService.class);

    private final MeasurementDao measurementDao;
    private final InvoiceDao invoiceDao;
    private final ImageService imageService;
    private final DetectService detectService;

    public MeasurementService(MeasurementDao measurementDao, InvoiceDao invoiceDao, ImageService imageService,
                              DetectService detectService) {
        this.measurementDao = measurementDao;
        this.invoiceDao = invoiceDao;
        this.imageService = imageService;
        this.detectService = detectService;
    }

    public void insertMeasurement(MultipartFile source, User user) throws IOException {
        String requestId = UUID.randomUUID().toString();
        String meterImage = imageService.scaleAndSave(requestId, source);
        boolean isMeter = detectService.hasMeter(requestId, meterImage);
        if (!isMeter) {
            throw new MeasurementException("На изображении нет счётчика. Пожалуйста, сфотографируйте счётчик " +
                    "или попробуйте ещё раз.", null);
        }

        String meter = detectService.readMeter(requestId, meterImage);
        logger.info("{}: read meter value: {}", requestId, meter);
        if (meter == null) {
            throw new MeasurementException("Счётчик не может быть прочитан", meter);
        }

        try {
            measurementDao.create(user.id(), meter);

            // Calculate due payment
            Long spent = null;
            List<Measurement> measurements = measurementDao.getUserMeasurements(user.id(), 2);
            if (measurements.size() == 2) {
                Long m1 = parseMeasurement(measurements.get(0).measurement());
                Long m2 = parseMeasurement(measurements.get(1).measurement());
                if (m1 != null && m2 != null) {
                    spent = m1 - m2;
                }
            } else if (measurements.size() == 1) {
                spent = parseMeasurement(measurements.getFirst().measurement());
            } else {
                throw new MeasurementException("Показания не найдены", meter);
            }

            if (spent == null || spent < 0) {
                // Only certified inspector can decrease measurement
                throw new MeasurementException("Новые показания меньше старых", meter);
            }

            // Create an unpaid invoice
            BigDecimal total = user.tariff().multiply(new BigDecimal(spent));
            invoiceDao.create(user.id(), total);
        } catch (MeasurementException exc) {
            logger.info("{}: failed with {}, read {}", requestId, exc.getLocalizedMessage(), exc.getMeter());
            throw exc;
        } catch (Exception exc) {
            logger.error("{}: can not process measurement", requestId, exc);
            throw new MeasurementException("Не получилось обработать показания", meter);
        }
    }

    private Long parseMeasurement(String measurement) {
        try {
            return Long.valueOf(measurement);
        } catch (NumberFormatException exc) {
            return null;
        }
    }

    public static class MeasurementException extends RuntimeException {
        private final String meter;

        public MeasurementException(String message, String meter) {
            super(message);
            this.meter = meter;
        }

        public String getMeter() {
            return meter;
        }
    }
}
