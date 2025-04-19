@import ru.capybarovsk.overhaul.model.User

@param User user
@param String recaptchaKey

@template.base.authenticated(
active = "measurements",
user = user,
title = "Передача показаний",
content = @`
    <h1>Передать показания</h1>
    <p>Для передачи показаний вам необходимо снять показания со счётчиков водоснабжения на камеру и прикрепить изображение ниже.</p>
    <p>Цифры должны быть чётко видны на изображении без бликов во избежание некорректных начислений.</p>
    <p>Рекомендуем использовать данный сервис на мобильном телефоне.</p>
    <p>На фотографии должен быть виден только один счётчик. Не отправляйте фотографии, на которых нет счётчиков.</p>

        <style>
            #cameraDiv, #uploadDiv { margin-top: 10px; }
        </style>
    <label>
        <input type="radio" name="photoSource" value="upload" checked>
        Загрузить фотографию счётчика
    </label>
    <label>
        <input type="radio" name="photoSource" value="camera">
        Снять счётчик на камеру
    </label>

    <div id="uploadDiv">
        <input type="file" id="uploadInput" accept="image/*">
    </div>

    <div id="cameraDiv" style="display: none;">
        <video id="video" autoplay playsinline style="max-width: 100%;" width="800" height="600"></video>
        <canvas id="canvas" style="display: none;"></canvas>
        <button id="captureBtn">Сделать фото</button>
    </div>

    <script src="https://www.google.com/recaptcha/api.js" async defer></script>
    <div class="g-recaptcha" data-sitekey="${recaptchaKey}"></div>
    <br>
    <button id="submitBtn">Передать</button>

    <script src="/assets/capture.js"></script>
`
)