/**
 * @template {Element} T
 * @param {string} id
 * @param {{new(): T}} constructor
 */
export function find(id, constructor) {
  const element = document.getElementById(id);
  if (!(element instanceof constructor)) throw new Error(`Missing element ${id}`);
  return element;
}

/** @param {HTMLCanvasElement} canvas @param {import("./protocol").Face} face @param {import("./protocol").Hands} hands */
export function draw(canvas, face, hands) {
  const context = canvas.getContext("2d");
  if (!context) throw new Error("Canvas unavailable.");
  const pen = context;
  const { width, height } = canvas;
  const scale = Math.min(width, height) / (2 * face.half_width);
  /** @param {import("./protocol").Point} point @returns {[number, number]} */
  const pixel = ([x, y]) => [width / 2 + x * scale, height / 2 - y * scale];
  /** @param {import("./protocol").Line[]} lines @param {string} color */
  function strokes(lines, color) {
    pen.strokeStyle = color;
    pen.lineWidth = 1.5;
    pen.beginPath();
    for (const [start, end] of lines) {
      pen.moveTo(...pixel(start));
      pen.lineTo(...pixel(end));
    }
    pen.stroke();
  }
  /** @param {import("./protocol").Point[]} points @param {string} color */
  function polygon(points, color) {
    pen.fillStyle = color;
    pen.beginPath();
    pen.moveTo(...pixel(points[0]));
    for (const point of points.slice(1)) pen.lineTo(...pixel(point));
    pen.closePath();
    pen.fill();
  }

  pen.clearRect(0, 0, width, height);
  face.rings.forEach((radius, index) => {
    pen.fillStyle = index === 0 ? "#ff0000" : "#ffffff";
    pen.beginPath();
    pen.arc(width / 2, height / 2, radius * scale, 0, 2 * Math.PI);
    pen.fill();
  });
  strokes(face.hours, "#ffffff");
  strokes(face.minutes, "#000000");
  strokes([hands.second], "#000000");
  polygon(hands.minute, "#00ff00");
  polygon(hands.hour, "#ffff00");
}
