function loadCredentials() {
  fetch("{{CONTEXT}}/rest/credentials")
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status} ${response.statusText}`);
      }
      return response.json();
    })
    .then((data) => {
      const container = document.getElementById("outCredentials");
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
      alert("load credentials");
    });
}

function deleteCredentials(id) {
  alert("to be implemented");
}
