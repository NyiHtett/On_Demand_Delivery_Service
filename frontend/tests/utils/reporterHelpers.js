import { test } from '@playwright/test';

export async function logPass(message) {
  console.log(`PASS: ${message}`);
  test.info().annotations.push({ type: 'PASS', description: message });
}

export async function logWarning(message) {
  console.warn(`WARNING: ${message}`);
  test.info().annotations.push({ type: 'WARNING', description: message });
}

export async function logFail(message) {
  console.error(`FAIL: ${message}`);
  test.info().annotations.push({ type: 'FAIL', description: message });
}

export async function logInfo(message) {
  console.log(`INFO: ${message}`);
  test.info().annotations.push({ type: 'INFO', description: message });
}

//screenshot requires page, attaching it to the html report requires testInfo, name is the name of the screenshot
export async function screenshot(page, testInfo, name) {
  const screenshotBuffer = await page.screenshot();
  await testInfo.attach(name, {
    body: screenshotBuffer,
    contentType: 'image/png',
  });
}
