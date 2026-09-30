// Future user API requests belong here.
async function signUpUser(signUpData) {
  const response = await fetch("/api/users/signup", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(signUpData)
  });

  if (!response.ok) {
    const details = await response.text();
    throw new Error(details || `Signup failed (HTTP ${response.status}).`);
  }

  return response.json();
}

async function loginUser(loginData) {
  const response = await fetch("/api/users/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(loginData)
  });

  if (!response.ok) {
    const details = await response.text();
    throw new Error(details || `Login failed (HTTP ${response.status}).`);
  }

  return response.json();
}

export { loginUser, signUpUser };