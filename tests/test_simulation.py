import csv
import subprocess
import tempfile
import unittest
from pathlib import Path

from web.simulation import Point, face, hands

ROOT = Path(__file__).resolve().parents[1]


def pixels(points: tuple[Point, ...]) -> list[int]:
    return [
        coordinate
        for x, y in points
        for coordinate in (int((x + 1.2) * 125), int((1.2 - y) * 125))
    ]


class ClockTest(unittest.TestCase):
    def test_geometry_matches_original_java_drawing_commands(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            subprocess.run(
                [
                    "javac",
                    "--release",
                    "25",
                    "-d",
                    directory,
                    ROOT / "clock/Clock.java",
                    *sorted((ROOT / "myutil").glob("*.java")),
                    ROOT / "tests/ClockReference.java",
                ],
                check=True,
                timeout=60,
            )
            dial = face()
            for hour, minute, second in (
                (0, 0, 0),
                (3, 7, 21),
                (15, 47, 20),
                (23, 59, 59),
            ):
                with self.subTest(time=(hour, minute, second)):
                    output = subprocess.check_output(
                        [
                            "java",
                            "-Djava.awt.headless=true",
                            "-cp",
                            directory,
                            "clock.ClockReference",
                            str(hour),
                            str(minute),
                            str(second),
                        ],
                        text=True,
                        timeout=20,
                    )
                    records = list(csv.reader(output.splitlines()))
                    self.assertEqual(len(records), 65)
                    self.assertEqual(
                        records[0],
                        ["ring", "16711680", "24", "24", "251", "251", "0", "360"],
                    )
                    self.assertEqual(
                        records[1],
                        ["ring", "16777215", "37", "37", "225", "225", "0", "360"],
                    )
                    sample = hands(hour, minute, second)
                    shapes = [
                        *(("line", 0xFFFFFF, tick) for tick in dial["hours"]),
                        *(("line", 0, tick) for tick in dial["minutes"]),
                        ("line", 0, sample["second"]),
                        ("triangle", 0x00FF00, sample["minute"]),
                        ("triangle", 0xFFFF00, sample["hour"]),
                    ]
                    for (kind, color, shape), row in zip(
                        shapes, records[2:], strict=True
                    ):
                        self.assertEqual(row[:2], [kind, str(color)])
                        for actual, expected in zip(
                            pixels(shape), map(int, row[2:]), strict=True
                        ):
                            self.assertLessEqual(abs(actual - expected), 1)

    def test_rejects_invalid_local_time(self) -> None:
        for invalid in ((24, 0, 0), (12, -1, 0), (12, 30, 60), (True, 0, 0)):
            with (
                self.subTest(time=invalid),
                self.assertRaisesRegex(ValueError, "local time"),
            ):
                hands(*invalid)
