@import java.text.DateFormat
@import java.util.List
@import ru.capybarovsk.overhaul.model.Measurement
@import ru.capybarovsk.overhaul.model.User

@param List<Measurement> measurements
@param User user

!{var dateFormatter = DateFormat.getDateTimeInstance();}

@template.base.authenticated(
    active = "measurements",
    user = user,
    title = "Показания",
    content = @`
    <h1>Ваши показания</h1>

    @if(!measurements.isEmpty())
        <p><a href="/measurements/new">Передать показания</a></p>
        <table>
            <thead>
            <tr>
                <th scope="col">Дата</th>
                <th scope="col">Значение счётчика</th>
            </tr>
            </thead>
            <tbody>
            @for(Measurement measurement: measurements)
                <tr>
                    <th scope="row">${dateFormatter.format(measurement.createdAt())}</th>
                    <th>${measurement.measurement()} м³</th>
                </tr>
            @endfor
            </tbody>
        </table>

        <p>Передавайте показания до 25 числа каждого месяца.</p>
    @else
        <p>Показаний пока нет. <a href="/measurements/new">Передайте показания</a> до 25 числа месяца.</p>
    @endif
`
)