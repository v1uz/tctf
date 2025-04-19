const uploadDiv = document.getElementById('uploadDiv');
const cameraDiv = document.getElementById('cameraDiv');
const radios = document.querySelectorAll('input[name="photoSource"]');
const video = document.getElementById('video');
const canvas = document.getElementById('canvas');
const captureBtn = document.getElementById('captureBtn');
const submitBtn = document.getElementById('submitBtn');

let stream = null;
let capturedFile = null;

radios.forEach(radio => {
  radio.addEventListener('change', () => {
    if (radio.value === 'upload' && radio.checked) {
      uploadDiv.style.display = 'block';
      cameraDiv.style.display = 'none';
      stopCamera();
    } else if (radio.value === 'camera' && radio.checked) {
      uploadDiv.style.display = 'none';
      cameraDiv.style.display = 'block';
      resetCaptureUI();
      startCamera();
    }
  });
});

function startCamera() {
  if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
    navigator.mediaDevices.getUserMedia({ video: { facingMode: { ideal: 'environment' } } })
      .then(s => {
        stream = s;
        video.srcObject = stream;
        video.style.display = 'block';
        canvas.style.display = 'none';
        captureBtn.textContent = 'Сделать фото';
      })
      .catch(err => console.error("Нет доступа к камере: ", err));
  } else {
    alert('Этот браузер не поддерживает видеосъёмку. Загрузите фото или обновите браузер.');
  }
}

function stopCamera() {
  if (stream) {
    stream.getTracks().forEach(track => track.stop());
    stream = null;
  }
}

function resetCaptureUI() {
  capturedFile = null;
  video.style.display = 'block';
  canvas.style.display = 'none';
  captureBtn.textContent = 'Сделать фото';
}

captureBtn.addEventListener('click', () => {
  if (captureBtn.textContent === 'Сделать фото') {
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const context = canvas.getContext('2d');
    context.drawImage(video, 0, 0);

    stopCamera();
    video.style.display = 'none';
    canvas.style.display = 'block';

    canvas.toBlob(blob => {
      capturedFile = new File([blob], 'captured.jpg', { type: 'image/jpeg' });
      alert("Photo captured!");
    }, 'image/jpeg');

    captureBtn.textContent = 'Переснять';
  } else if (captureBtn.textContent === 'Переснять') {
    resetCaptureUI();
    startCamera();
  }
});

submitBtn.addEventListener('click', () => {
  const formData = new FormData();

  const recaptchaResponse = grecaptcha.getResponse();
  if (!recaptchaResponse) {
    alert("Подтвердите, что вы не робот");
    return;
  }

  formData.append('recaptchaResponse', recaptchaResponse);

  const selectedSource = document.querySelector('input[name="photoSource"]:checked').value;
  if (selectedSource === 'upload') {
    const fileInput = document.getElementById('uploadInput');
    if (fileInput.files.length === 0) {
      alert("Прикрепите фото");
      return;
    }
    formData.append('photo', fileInput.files[0]);
  } else if (selectedSource === 'camera') {
    if (!capturedFile) {
      alert("Нажмите «Снять фото», чтобы снять счётчик");
      return;
    }
    formData.append('photo', capturedFile);
  }

  submitBtn.disabled = true;
  submitBtn.innerText = 'Данные в обработке';

  fetch('/measurements/new', {
    method: 'POST',
    body: formData
  })
  .then(response => {
    if (response.status !== 200) {
      return response.text();
    } else {
      window.location.href = '/measurements';
    }
  })
  .then(data => {
    if (data) {
      alert("Ошибка! " + data);
      window.location.reload();
    }
  })
  .catch(err => {
    console.error("Error during upload:", err);
    alert("Не удалось загрузить файл!");
  });
});