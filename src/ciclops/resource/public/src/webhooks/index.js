function loadWebhooks() {
  const container = document.getElementById("outWebhooks");
  container.innerHTML = "";
  fetch(`{{CONTEXT}}/rest/webhooks`)
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status} ${response.statusText}`);
      }
      return response.json();
    })
    .then((webhookData) => {
      webhookData["webhooks"].forEach(webhook => {
        const li = document.createElement("li");
        li.innerText = webhook.name;

        const bttnDelete = document.createElement("button");
        bttnDelete.innerText = "x";
        bttnDelete.addEventListener("click", () =>
          deleteWebhook(webhook.id)
        );
        li.appendChild(bttnDelete);
        container.appendChild(li);
      });
    })
    .catch((e) => {
      console.error(e);
      alert("failed to create webhook");
    });
}

function createWebhook() {
  const webhookName = document.getElementById("inputName").value;
  const webhookURL = document.getElementById("inputURL").value;

  fetch(`{{CONTEXT}}/rest/webhooks`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      name: webhookName,
      url: webhookURL,
    }),
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status} ${response.statusText}`);
      }
      loadWebhooks();
      document.getElementById("inputName").value = "";
      document.getElementById("inputURL").value = "";
    })
    .catch((e) => {
      console.error(e);
      alert("failed to create webhook");
    });
}

function deleteWebhook(id) {
  fetch(`{{CONTEXT}}/rest/webhooks/id/${id}`, {
    method: "delete",
  })
    .then((response) => {
      if (!response.status !== 204) {
        throw new Error(`HTTP ${response.status} ${response.statusText}`);
      }
      loadCredentials();
    })
    .catch((e) => {
      console.error(e);
      alert("could not delete webhook");
    });
}
