import threading
import unittest
from datetime import UTC, datetime
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

from playwright.sync_api import (
    Browser,
    BrowserContext,
    Page,
    Playwright,
    expect,
    sync_playwright,
)

BUILD = Path(__file__).resolve().parents[1] / "build"


class BrowserTest(unittest.TestCase):
    server: ThreadingHTTPServer
    thread: threading.Thread
    playwright: Playwright
    browser: Browser
    context: BrowserContext
    page: Page

    @classmethod
    def setUpClass(cls) -> None:
        if not (BUILD / "site/index.html").is_file():
            raise RuntimeError("Build the site before running browser tests.")
        # Serve from a subdirectory to match a GitHub project Pages URL.
        cls.server = ThreadingHTTPServer(
            ("127.0.0.1", 0), partial(SimpleHTTPRequestHandler, directory=BUILD)
        )
        cls.thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.thread.start()
        cls.addClassCleanup(cls.server.server_close)
        cls.addClassCleanup(cls.thread.join, 5)
        cls.addClassCleanup(cls.server.shutdown)
        cls.playwright = sync_playwright().start()
        cls.addClassCleanup(cls.playwright.stop)
        cls.browser = cls.playwright.chromium.launch()
        cls.addClassCleanup(cls.browser.close)

    def setUp(self) -> None:
        self.context = self.browser.new_context()
        self.addCleanup(self.context.close)
        self.page = self.context.new_page()
        self.base_url = f"http://127.0.0.1:{self.server.server_port}/site/"

    def test_browser_clock_uses_local_time_and_updates(self) -> None:
        page = self.browser.new_page(locale="en-US", timezone_id="America/New_York")
        self.addCleanup(page.close)
        page.clock.set_fixed_time(datetime(2026, 2, 5, 20, 47, 20, tzinfo=UTC))
        page.goto(self.base_url)
        clock = page.locator("#clock")
        time = page.locator("#time")
        expect(time).to_have_text("3:47:20 PM", timeout=45_000)
        expect(time).to_have_attribute("datetime", "2026-02-05T20:47:20.000Z")
        expect(page.locator("#zone")).to_have_text("America/New York")
        before = clock.evaluate("(canvas) => canvas.toDataURL()")
        page.clock.set_fixed_time(datetime(2026, 2, 5, 20, 47, 21, tzinfo=UTC))
        expect(time).to_have_text("3:47:21 PM")
        self.assertNotEqual(clock.evaluate("(canvas) => canvas.toDataURL()"), before)
        page.set_viewport_size({"width": 390, "height": 844})
        self.assertTrue(
            page.evaluate("document.documentElement.scrollWidth <= innerWidth")
        )

    def test_runtime_failure_shows_retry(self) -> None:
        self.page.route("**/vendor/pyodide.js", lambda route: route.abort())
        self.page.goto(self.base_url)
        expect(self.page.get_by_role("alert")).to_contain_text("Python could not load")
        expect(self.page.get_by_role("button", name="Reload")).to_be_visible()
