from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait

from selenium_utils import wait_for_visible, fill_input, click_button_by_text


def test_login_success(driver, base_url, admin_credentials):
    driver.execute_script('localStorage.clear(); sessionStorage.clear();')
    driver.delete_all_cookies()
    driver.get(base_url + '/login')

    wait_for_visible(driver, By.XPATH, "//h2[normalize-space()='Connexion']")

    fill_input(driver, 'Email', admin_credentials['email'])
    fill_input(driver, 'Mot de passe', admin_credentials['password'])
    click_button_by_text(driver, 'Se connecter')

    WebDriverWait(driver, 15).until(
        lambda drv: '/dashboard' in drv.current_url or '/system/dashboard' in drv.current_url
    )
    wait_for_visible(driver, By.XPATH, "//nav//a[normalize-space()='Membres']")
