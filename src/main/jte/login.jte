@param String error

@template.base.guest(title = "Вход", content = @`
    <div class="form-container">
        <form method="POST">
            <h1>Авторизуйтесь в КапиЖилСервисЦентре</h1>
            @if(error != null)
                <p class="error" id="errorMsg">${error}</p>
            @endif
            <fieldset>
                <div>
                    <label for="login">Номер лицевого счёта</label>
                    <input type="text" minlength="9" pattern="[0-9]{9,}" id="login" name="login" required
                        placeholder="Девятизначное число">
                </div>
                <div>
                    <label for="password">Пароль</label>
                    <input type="password" id="password" name="password" required>
                </div>
                <button type="submit">Войти</button>
            </fieldset>
        </form>
        <p style="text-align: center;"><a href="/account/register">Нет аккаунта?</a></p>
    </div>
`)