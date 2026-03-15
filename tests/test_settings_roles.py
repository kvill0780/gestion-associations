from selenium.webdriver.common.by import By

from selenium_utils import ensure_logged_in, wait_for_clickable, wait_for_visible


def test_roles_tab_accessible_for_admin(driver, base_url, admin_credentials):
    ensure_logged_in(driver, base_url, admin_credentials['email'], admin_credentials['password'])

    driver.get(base_url + '/settings')

    roles_tab = wait_for_clickable(driver, By.XPATH, "//button[normalize-space()='Rôles']")
    roles_tab.click()

    wait_for_visible(driver, By.XPATH, "//button[normalize-space()='Créer un rôle']")
