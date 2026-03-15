import os
import socket
from pathlib import Path
from urllib.parse import urlparse

import pytest
import shutil
from dotenv import load_dotenv
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.chrome.service import Service


ROOT_DIR = Path(__file__).resolve().parent
ENV_PATH = ROOT_DIR / '.env'

if ENV_PATH.exists():
    load_dotenv(ENV_PATH)
else:
    load_dotenv(ROOT_DIR / '.env.example')


@pytest.fixture(scope='session')
def base_url():
    return os.getenv('BASE_URL', 'http://localhost:5174').rstrip('/')


@pytest.fixture(scope='session')
def api_base_url():
    return os.getenv('API_BASE_URL', 'http://localhost:8080').rstrip('/')


@pytest.fixture(scope='session')
def admin_credentials():
    return {
        'email': os.getenv('ADMIN_EMAIL', 'president.test@associa.bf'),
        'password': os.getenv('ADMIN_PASSWORD', 'password')
    }


@pytest.fixture(scope='session')
def treasurer_credentials():
    return {
        'email': os.getenv('TREASURER_EMAIL', 'treasurer.test@associa.bf'),
        'password': os.getenv('TREASURER_PASSWORD', 'password')
    }


@pytest.fixture(scope='session')
def member_credentials():
    return {
        'email': os.getenv('MEMBER_EMAIL', 'member1@test.associa.bf'),
        'password': os.getenv('MEMBER_PASSWORD', 'password')
    }


def _service_reachable(url, timeout_seconds=2):
    parsed = urlparse(url)
    host = parsed.hostname or 'localhost'
    port = parsed.port
    if port is None:
        port = 443 if parsed.scheme == 'https' else 80

    try:
        with socket.create_connection((host, port), timeout=timeout_seconds):
            return True
    except OSError:
        return False


@pytest.fixture(scope='session')
def driver(base_url, api_base_url):
    options = Options()
    headless = os.getenv('HEADLESS', 'true').lower() in {'1', 'true', 'yes'}
    if headless:
        options.add_argument('--headless=new')
    options.add_argument('--window-size=1280,900')
    options.add_argument('--disable-gpu')
    options.add_argument('--no-sandbox')
    options.add_argument('--disable-dev-shm-usage')
    chrome_binary = os.getenv('CHROME_BINARY')
    if chrome_binary:
        options.binary_location = chrome_binary

    if not _service_reachable(base_url):
        pytest.skip(f'Frontend indisponible: {base_url}')
    if not _service_reachable(api_base_url):
        pytest.skip(f'Backend indisponible: {api_base_url}')

    driver_path = os.getenv('CHROMEDRIVER_PATH')
    if driver_path and Path(driver_path).exists():
        service = Service(driver_path)
    else:
        path_in_system = shutil.which('chromedriver')
        if path_in_system:
            service = Service(path_in_system)
        else:
            allow_download = os.getenv('ALLOW_DRIVER_DOWNLOAD', 'false').lower() in {'1', 'true', 'yes'}
            if not allow_download:
                pytest.skip(
                    'ChromeDriver introuvable. Installe chromedriver et définis CHROMEDRIVER_PATH '
                    'ou active ALLOW_DRIVER_DOWNLOAD=true.'
                )
            try:
                service = Service(ChromeDriverManager().install())
            except Exception:
                pytest.skip(
                    'ChromeDriver indisponible (download échoué). '
                    'Installe chromedriver et définis CHROMEDRIVER_PATH.'
                )

    driver = webdriver.Chrome(service=service, options=options)
    driver.implicitly_wait(5)
    yield driver
    driver.quit()
