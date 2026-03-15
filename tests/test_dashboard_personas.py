from selenium.webdriver.common.by import By

from selenium_utils import (
    has_text,
    login_fresh,
    wait_for_clickable,
    wait_for_visible
)


def _assert_text_absent(driver, text):
    assert not has_text(driver, text), f'Texte inattendu visible: {text}'


def test_dashboard_persona_admin_layout(driver, base_url, admin_credentials):
    login_fresh(driver, base_url, admin_credentials['email'], admin_credentials['password'])

    wait_for_visible(driver, By.XPATH, "//h2[contains(normalize-space(),'Tableau de bord')]")
    is_admin_persona = has_text(driver, 'Tableau de bord administrateur')
    is_finance_persona = has_text(driver, 'Tableau de bord financier')
    assert is_admin_persona or is_finance_persona, 'Persona inattendue pour le compte admin_credentials'

    if is_admin_persona:
        wait_for_visible(driver, By.XPATH, "//h3[normalize-space()='Membres en attente']")
        wait_for_visible(driver, By.XPATH, "//h3[normalize-space()='Évolution financière']")
        wait_for_visible(driver, By.XPATH, "//h3[normalize-space()='Actions rapides']")
        _assert_text_absent(driver, 'Transactions récentes')
    else:
        wait_for_visible(driver, By.XPATH, "//h3[normalize-space()='Évolution financière']")
        wait_for_visible(driver, By.XPATH, "//h3[normalize-space()='Budget mensuel']")
        wait_for_visible(driver, By.XPATH, "//h3[normalize-space()='Transactions récentes']")
        _assert_text_absent(driver, 'Membres en attente')

    wait_for_clickable(driver, By.XPATH, "//button[contains(normalize-space(),'Afficher plus')]").click()
    wait_for_visible(driver, By.XPATH, "//button[contains(normalize-space(),'Masquer les details')]")


def test_dashboard_persona_treasurer_layout(driver, base_url, treasurer_credentials):
    login_fresh(driver, base_url, treasurer_credentials['email'], treasurer_credentials['password'])

    wait_for_visible(driver, By.XPATH, "//h2[contains(normalize-space(),'Tableau de bord financier')]")
    wait_for_visible(driver, By.XPATH, "//h3[normalize-space()='Évolution financière']")
    wait_for_visible(driver, By.XPATH, "//h3[normalize-space()='Budget mensuel']")
    wait_for_visible(driver, By.XPATH, "//h3[normalize-space()='Transactions récentes']")

    _assert_text_absent(driver, 'Membres en attente')
    _assert_text_absent(driver, 'Actions rapides')

    wait_for_clickable(driver, By.XPATH, "//button[contains(normalize-space(),'Afficher plus')]").click()
    wait_for_visible(driver, By.XPATH, "//button[contains(normalize-space(),'Masquer les details')]")
    wait_for_visible(driver, By.XPATH, "//h3[normalize-space()='Actions rapides']")


def test_dashboard_persona_member_layout(driver, base_url, member_credentials):
    login_fresh(driver, base_url, member_credentials['email'], member_credentials['password'])

    wait_for_visible(driver, By.XPATH, "//h2[normalize-space()='Tableau de bord']")
    wait_for_visible(driver, By.XPATH, "//h3[contains(normalize-space(),'Bienvenue')]")
    wait_for_visible(driver, By.XPATH, "//h3[normalize-space()='Activités récentes']")

    _assert_text_absent(driver, 'Évolution financière')
    _assert_text_absent(driver, 'Budget mensuel')
    _assert_text_absent(driver, 'Actions rapides')
    _assert_text_absent(driver, 'Membres en attente')
    _assert_text_absent(driver, 'Afficher plus')
