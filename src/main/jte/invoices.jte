@import java.util.List
@import java.text.DateFormat
@import ru.capybarovsk.overhaul.model.Invoice
@import ru.capybarovsk.overhaul.model.User

@param List<Invoice> invoices
@param User user

!{var dateFormatter = DateFormat.getDateTimeInstance();}

@template.base.authenticated(
    active = "invoices",
    user = user,
    title = "Счета",
    content = @`
    <h1>Ваши счета</h1>

    @if(!invoices.isEmpty())
    <table>
        <thead>
            <tr>
                <th scope="col">Дата</th>
                <th scope="col">Сумма</th>
                <th scope="col">Оплачено</th>
            </tr>
        </thead>
        <tbody>
            @for(Invoice invoice: invoices)
            <tr>
                <th scope="row">${dateFormatter.format(invoice.createdAt())}</th>
                <th>${invoice.total()} ₡</th>
                <th>
                    @if(invoice.paid())
                        Оплачено
                    @else
                        К оплате
                    @endif
                </th>
            </tr>
            @endfor
        </tbody>
    </table>

    <p>Для оперативной оплаты счетов обратитесь в Единый расчётный центр с понедельника по среду с 7:45 до 9:18.</p>
    @else
        <p>Счетов пока нет. <a href="/measurements/new">Передайте показания</a>, чтобы рассчитать сумму к оплате.</p>
    @endif
`
)
