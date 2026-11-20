"""Geometry from Robert Vanderbei's original ClockCanvas."""

import json
from math import cos, pi, sin, tau
from typing import TypedDict

Point = tuple[float, float]
Line = tuple[Point, Point]


class Face(TypedDict):
    hours: list[Line]
    minutes: list[Line]
    rings: tuple[float, float]
    half_width: float


class Hands(TypedDict):
    second: Line
    minute: tuple[Point, Point, Point]
    hour: tuple[Point, Point, Point]


def angle(value: float, period: int) -> float:
    return pi / 2 - tau * value / period


def point(radius: float, theta: float) -> Point:
    return radius * cos(theta), radius * sin(theta)


def tick(index: int, period: int, inner_radius: float) -> Line:
    theta = angle(index, period)
    return point(inner_radius, theta), point(1.0, theta)


def face() -> Face:
    return {
        "rings": (1.0, 0.9),
        "half_width": 1.2,
        "hours": [tick(index, 12, 0.9) for index in range(12)],
        "minutes": [tick(index, 60, 0.94) for index in range(60) if index % 5],
    }


def triangle(value: float, period: int, radius: float) -> tuple[Point, Point, Point]:
    theta = angle(value, period)
    base = 0.1 * sin(theta), -0.1 * cos(theta)
    return base, (-base[0], -base[1]), point(radius, theta)


def hands(hour: int, minute: int, second: int) -> Hands:
    for value, limit in ((hour, 24), (minute, 60), (second, 60)):
        if type(value) is not int or not 0 <= value < limit:
            raise ValueError("Use a valid local time.")
    return {
        "second": ((0.0, 0.0), point(0.9, angle(second, 60))),
        "minute": triangle(minute + second / 60, 60, 0.8),
        "hour": triangle(hour + minute / 60 + second / 3600, 12, 0.4),
    }


def load_face() -> str:
    return json.dumps(face())


def sample(hour: int, minute: int, second: int) -> str:
    return json.dumps(hands(hour, minute, second))
