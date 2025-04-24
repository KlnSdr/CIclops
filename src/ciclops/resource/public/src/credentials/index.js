function loadCredentials() {
  const container = document.getElementById("outCredentials");
  container.innerHTML = "";
  fetch("{{CONTEXT}}/rest/credentials")
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status} ${response.statusText}`);
      }
      return response.json();
    })
    .then((data) => {
      Object.keys(data).forEach((key) => {
        const li = document.createElement("li");
        li.innerText = data[key];

        const bttnDelete = document.createElement("button");
        bttnDelete.innerText = "x";
        bttnDelete.addEventListener("click", () =>
          deleteCredentials(key.split("_")[1])
        );
        li.appendChild(bttnDelete);
        container.appendChild(li);
      });
    })
    .catch((e) => {
      console.error(e);
      container.innerText = "could not load credentials";
    });
}

function createCredentials() {
  const type = document.getElementById("selType").value;
  const host = document.getElementById("inputHost").value;
  const username = document.getElementById("inputUsername").value;
  const password = document.getElementById("inputPassword").value;

  fetch(`{{CONTEXT}}/rest/credentials/${type}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      host: host,
      username: username,
      password: password,
    }),
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status} ${response.statusText}`);
      }
      loadCredentials();
      document.getElementById("inputHost").value = "";
      document.getElementById("inputUsername").value = "";
      document.getElementById("inputPassword").value = "";
    })
    .catch((e) => {
      console.error(e);
      alert("failed to create credentials");
    });
}

function deleteCredentials(id) {
  fetch(`{{CONTEXT}}/rest/credentials/id/${id}`, {
    method: "delete",
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status} ${response.statusText}`);
      }
      loadCredentials();
    })
    .catch((e) => {
      console.error(e);
      alert("could not delete credentials");
    });
}
