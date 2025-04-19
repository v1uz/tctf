const form = document.getElementById('registrationForm');
const submitButton = document.getElementById('submitButton');
const errorMsg = document.getElementById('errorMsg');
const password = document.getElementById('password');
const confirmPassword = document.getElementById('confirmPassword');

function checkPassword(str) {
  if (str.length < 9) {
    return false;
  }

  if (!/[A-Z]/.test(str)) {
    return false;
  }

  if (!/[a-z]/.test(str)) {
    return false;
  }

  if (!/\d/.test(str)) {
    return false;
  }

  return true;
}

// Function to validate the form
function validateForm() {
  errorMsg.textContent = '';
  let valid = true;

  if (!password.value) {
    valid = false;
  }

  if (password.value !== confirmPassword.value) {
    valid = false;
    errorMsg.textContent = 'Пароли не совпадают';
  }

  if (password.value && !checkPassword(password.value)) {
    valid = false;
    errorMsg.textContent = 'Пароль должен удовлетворять условиям: не менее 9 символов, наличие заглавной буквы, наличие строчной буквы, наличие цифры'
  }

  submitButton.disabled = !valid;
}

form.addEventListener('input', validateForm);

form.addEventListener('submit', function(e) {
  if (submitButton.disabled) {
    e.preventDefault();
  }
});