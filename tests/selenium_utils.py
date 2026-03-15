from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException


def wait_for_visible(driver, by, value, timeout=10):
    return WebDriverWait(driver, timeout).until(
        EC.visibility_of_element_located((by, value))
    )


def wait_for_clickable(driver, by, value, timeout=10):
    return WebDriverWait(driver, timeout).until(
        EC.element_to_be_clickable((by, value))
    )


def wait_for_text(driver, by, value, text, timeout=10):
    def _predicate(drv):
        element = drv.find_element(by, value)
        return text in element.text
    return WebDriverWait(driver, timeout).until(_predicate)


def wait_for_url_contains(driver, fragment, timeout=10):
    return WebDriverWait(driver, timeout).until(lambda drv: fragment in drv.current_url)


def fill_input(driver, label_text, value):
    label = driver.find_element(By.XPATH, f"//label[normalize-space()='{label_text}']")
    input_el = label.find_element(By.XPATH, "following-sibling::input")
    input_el.clear()
    input_el.send_keys(value)


def click_button_by_text(driver, text):
    btn = driver.find_element(By.XPATH, f"//button[normalize-space()='{text}']")
    btn.click()


def click_nav_link(driver, text):
    link = wait_for_clickable(driver, By.XPATH, f"//nav//a[normalize-space()='{text}']")
    link.click()


def wait_for_heading_contains(driver, text, timeout=10):
    return wait_for_visible(driver, By.XPATH, f"//h2[contains(normalize-space(),'{text}')]", timeout=timeout)


def ensure_logged_in(driver, base_url, email, password):
    driver.get(base_url + '/login')
    try:
        wait_for_visible(driver, By.XPATH, "//h2[normalize-space()='Connexion']", timeout=3)
        fill_input(driver, 'Email', email)
        fill_input(driver, 'Mot de passe', password)
        click_button_by_text(driver, 'Se connecter')
        WebDriverWait(driver, 15).until(
            lambda drv: '/dashboard' in drv.current_url or '/system/dashboard' in drv.current_url
        )
    except TimeoutException:
        # Déjà connecté (ou redirigé) : vérifier l'existence du menu
        wait_for_visible(driver, By.XPATH, "//nav//a[normalize-space()='Membres']", timeout=10)


def login_fresh(driver, base_url, email, password):
    # Navigate first to ensure storage APIs are available on an HTTP origin.
    driver.get(base_url + '/login')
    try:
        driver.execute_script('window.localStorage.clear(); window.sessionStorage.clear();')
    except Exception:
        # Some drivers may still start on a restricted page; cookies cleanup is enough fallback.
        pass
    driver.delete_all_cookies()
    driver.get(base_url + '/login')

    wait_for_visible(driver, By.XPATH, "//h2[normalize-space()='Connexion']")
    fill_input(driver, 'Email', email)
    fill_input(driver, 'Mot de passe', password)
    click_button_by_text(driver, 'Se connecter')

    WebDriverWait(driver, 15).until(
        lambda drv: '/dashboard' in drv.current_url or '/system/dashboard' in drv.current_url
    )


def has_text(driver, text):
    return len(driver.find_elements(By.XPATH, f"//*[contains(normalize-space(),\"{text}\")]")) > 0
