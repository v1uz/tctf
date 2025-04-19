@import gg.jte.Content
@import ru.capybarovsk.overhaul.model.User

@param User user
@param String title
@param Content content
@param String active = null

@template.base.layout(
    title = title,
    header = @`
    <link rel="stylesheet" href="/assets/zhil.css" />
    `,
    content = @`
    <nav>
        <ul>
            <li><strong>КапиЖилСервисЦентр</strong></li>
        </ul>
        <ul>
            @template.util.link(active = active, slug = "invoices", href = "/invoices", value = "Счета")
            @template.util.link(active = active, slug = "measurements", href = "/measurements", value = "Показания")
            <li title="${user.clientAddress()}">${user.login()}</li>
            <li><a href="/account/logout">Выйти</a></li>
        </ul>
    </nav>
    <main>
    ${content}
    </main>
    <footer>
        All rights reserved 2025 КапиЖилСервисЦентр
    </footer>
    `
)
