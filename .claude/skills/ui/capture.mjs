// Captures d'écran sans outil de navigateur : pilote Chrome headless par le protocole DevTools.
// Prérequis : Node 22+, Google Chrome, frontend lancé (et backend pour les écrans connectés).
//
//   node .claude/skills/ui/capture.mjs <dossier-de-sortie> <chemin> [<chemin>...]
//   UI_EMAIL=... UI_PASSWORD=... node .claude/skills/ui/capture.mjs <dossier> /app /app/admin/users
//
// Variables : UI_BASE (défaut http://localhost:4200), UI_EMAIL et UI_PASSWORD (connexion préalable),
// UI_PROFILE (libellé du profil à choisir si l'écran de choix s'affiche), CHROME (chemin du binaire).
// Chaque écran est capturé à 1280, 768 et 375 px ; le débordement horizontal de la page est mesuré.
import { spawn } from 'node:child_process';
import { mkdirSync, mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

const [out, ...paths] = process.argv.slice(2);
if (!out || paths.length === 0) {
  console.error('Usage : node capture.mjs <dossier-de-sortie> <chemin> [<chemin>...]');
  process.exit(1);
}
const BASE = process.env.UI_BASE ?? 'http://localhost:4200';
const CHROME = process.env.CHROME ?? '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const WIDTHS = [1280, 768, 375];
const PORT = 9333;
mkdirSync(out, { recursive: true });

const chrome = spawn(CHROME, [
  '--headless=new', '--disable-gpu', `--remote-debugging-port=${PORT}`,
  `--user-data-dir=${mkdtempSync(join(tmpdir(), 'ui-capture-'))}`, 'about:blank',
]);
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

let page;
for (let attempt = 0; attempt < 40 && !page; attempt++) {
  await sleep(250);
  try {
    page = (await (await fetch(`http://localhost:${PORT}/json`)).json()).find((target) => target.type === 'page');
  } catch { /* Chrome démarre encore */ }
}
if (!page) {
  chrome.kill();
  console.error('Chrome headless injoignable : vérifier la variable CHROME.');
  process.exit(1);
}

const socket = new WebSocket(page.webSocketDebuggerUrl);
await new Promise((resolve) => (socket.onopen = resolve));
let sequence = 0;
const pending = new Map();
socket.onmessage = (event) => {
  const message = JSON.parse(event.data);
  pending.get(message.id)?.(message);
  pending.delete(message.id);
};
const send = (method, params = {}) =>
  new Promise((resolve) => {
    const id = ++sequence;
    pending.set(id, resolve);
    socket.send(JSON.stringify({ id, method, params }));
  });
const evaluate = async (expression) =>
  (await send('Runtime.evaluate', { expression, awaitPromise: true, returnByValue: true })).result?.result?.value;
const setWidth = (width) =>
  send('Emulation.setDeviceMetricsOverride', { width, height: 900, deviceScaleFactor: 1, mobile: false });
const open = async (path) => {
  await send('Page.navigate', { url: BASE + path });
  await sleep(1500);
};
const fill = (selector, value) =>
  evaluate(`(() => {
    const field = document.querySelector(${JSON.stringify(selector)});
    field.value = ${JSON.stringify(value)};
    field.dispatchEvent(new Event('input', { bubbles: true }));
  })()`);

await send('Page.enable');
await setWidth(1280);

if (process.env.UI_EMAIL) {
  await open('/auth/login');
  await fill('#login-email', process.env.UI_EMAIL);
  await fill('#login-password', process.env.UI_PASSWORD ?? '');
  await evaluate(`document.querySelector('button[type=submit]').click()`);
  await sleep(1500);
  if (process.env.UI_PROFILE && (await evaluate('location.pathname')).includes('select-profile')) {
    await evaluate(`[...document.querySelectorAll('button')]
      .find((button) => button.textContent.trim().startsWith(${JSON.stringify(process.env.UI_PROFILE)}))?.click()`);
    await sleep(1500);
  }
  console.log('Après connexion :', await evaluate('location.pathname'));
}

for (const path of paths) {
  await open(path);
  const name = path.replace(/^\/+|\/+$/g, '').replace(/[^a-zA-Z0-9]+/g, '-') || 'accueil';
  for (const width of WIDTHS) {
    await setWidth(width);
    await sleep(300);
    const overflow = await evaluate('document.documentElement.scrollWidth - document.documentElement.clientWidth');
    const { result } = await send('Page.captureScreenshot', { format: 'png', captureBeyondViewport: true });
    writeFileSync(join(out, `${name}_${width}.png`), Buffer.from(result.data, 'base64'));
    console.log(`${path} — ${width} px — débordement horizontal : ${overflow} px — affiché : ${await evaluate('location.pathname')}`);
  }
  await setWidth(1280);
}

socket.close();
chrome.kill();
