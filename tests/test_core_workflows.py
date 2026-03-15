from selenium.webdriver.common.by import By

from selenium_utils import (
    ensure_logged_in,
    wait_for_heading_contains,
    wait_for_url_contains,
    wait_for_visible,
    click_nav_link,
    click_button_by_text
)


def test_core_navigation_pages(driver, base_url, admin_credentials):
    ensure_logged_in(driver, base_url, admin_credentials['email'], admin_credentials['password'])

    click_nav_link(driver, 'Tableau de bord')
    wait_for_heading_contains(driver, 'Tableau de bord')

    click_nav_link(driver, 'Événements')
    wait_for_heading_contains(driver, 'Événements')
    wait_for_visible(driver, By.XPATH, "//button[normalize-space()='Créer']")

    click_nav_link(driver, 'Finances')
    wait_for_heading_contains(driver, 'Finances')
    wait_for_visible(driver, By.XPATH, "//button[normalize-space()='Nouvelle transaction']")

    click_nav_link(driver, 'Paramètres')
    wait_for_heading_contains(driver, 'Paramètres')
    wait_for_visible(driver, By.XPATH, "//button[normalize-space()='Profil']")


def test_profile_password_form_validation(driver, base_url, admin_credentials):
    ensure_logged_in(driver, base_url, admin_credentials['email'], admin_credentials['password'])

    driver.get(base_url + '/settings')
    wait_for_heading_contains(driver, 'Paramètres')
    wait_for_visible(driver, By.XPATH, "//h3[normalize-space()='Sécurité']")

    click_button_by_text(driver, 'Changer le mot de passe')
    wait_for_visible(driver, By.XPATH, "//p[contains(normalize-space(), 'mot de passe actuel est requis')]")


def test_logout_redirects_to_login(driver, base_url, admin_credentials):
    ensure_logged_in(driver, base_url, admin_credentials['email'], admin_credentials['password'])

    click_button_by_text(driver, 'Déconnexion')
    wait_for_url_contains(driver, '/login', timeout=15)
    wait_for_visible(driver, By.XPATH, "//h2[normalize-space()='Connexion']")
