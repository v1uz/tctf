@param String error

@template.base.guest(title = "Регистрация", content = @`
    <div class="form-container">
        <form id="registrationForm" method="POST">
            <h1>Добавьте лицевой счёт</h1>
            <p>Все поля обязательны.</p>
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
                <div>
                    <label for="confirmPassword">Повторите пароль</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" required>
                    <span class="error" id="errorMsg"></span>
                </div>
                <div>
                    <label for="fullName">ФИО</label>
                    <input type="text" id="fullName" name="fullName" required>
                </div>
                <div>
                    <label for="address">Адрес объекта</label>
                    <input type="text" id="address" name="address" required>
                </div>
                <div>
                    <label for="privacy">
                        <input type="checkbox" id="privacy" name="privacy" required>
                        Даю согласие на обработку персональных данных
                    </label>
                </div>
                <button type="submit" id="submitButton" disabled>Зарегистрироваться</button>
            </fieldset>
        </form>
        <p style="text-align: center;"><a href="/account/login">Уже зарегистрированы?</a></p>
    </div>

    <script src="/assets/register.js"></script>
`)