import { draw, find } from "./view.js";

const canvas = find("clock", HTMLCanvasElement);
const status = find("status", HTMLElement);
const readout = find("readout", HTMLElement);
const time = find("time", HTMLTimeElement);
const zone = find("zone", HTMLElement);
const error = find("error", HTMLElement);
const retry = find("retry", HTMLButtonElement);
const formatter = new Intl.DateTimeFormat(undefined, { hour: "numeric", minute: "2-digit", second: "2-digit" });
const worker = new Worker(new URL("./worker.js", import.meta.url));
/** @type {import("./protocol").Face | undefined} */
let face;
let timer = 0;

function requestTime() {
  const now = new Date();
  worker.postMessage({
    hour: now.getHours(), minute: now.getMinutes(), second: now.getSeconds(),
    timestamp: now.getTime(),
  });
}

function fail() {
  window.clearTimeout(timer);
  worker.terminate();
  status.hidden = true;
  error.textContent = "Python could not load the clock. Reload to try again.";
  error.hidden = retry.hidden = false;
}

worker.onerror = fail;
worker.onmessage = (/** @type {MessageEvent<import("./protocol").Reply>} */ { data }) => {
  if (data.type === "ready") {
    face = data.face;
    zone.textContent = formatter.resolvedOptions().timeZone.replaceAll("_", " ");
    requestTime();
  } else if (data.type === "sample" && face) {
    draw(canvas, face, data.hands);
    const moment = new Date(data.timestamp);
    time.dateTime = moment.toISOString();
    time.textContent = formatter.format(moment);
    status.hidden = true;
    readout.hidden = false;
    const delay = 1000 - Date.now() % 1000;
    timer = window.setTimeout(requestTime, delay);
  } else fail();
};
retry.addEventListener("click", () => window.location.reload());
