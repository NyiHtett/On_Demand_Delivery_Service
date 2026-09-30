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

export {signUpUser};
