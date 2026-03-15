from selenium.webdriver.common.by import By

from selenium_utils import ensure_logged_in, wait_for_visible


def test_members_page_loads(driver, base_url, admin_credentials):
    ensure_logged_in(driver, base_url, admin_credentials['email'], admin_credentials['password'])

    driver.get(base_url + '/members')

    wait_for_visible(driver, By.XPATH, "//h2[normalize-space()='Membres']")
    wait_for_visible(driver, By.XPATH, "//input[@placeholder='Rechercher un membre...']")
    wait_for_visible(driver, By.XPATH, "//table")
