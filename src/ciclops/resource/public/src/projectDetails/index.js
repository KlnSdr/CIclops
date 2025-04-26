function triggerBuild(projectId) {
  fetch(`{{CONTEXT}}/rest/projects/id/${projectId}/trigger-build`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status} ${response.statusText}`);
      }
      alert("build scheduled");
    })
    .catch((e) => {
      console.error(e);
      alert("could not schedule build");
    });
}

function triggerBuildRelease(projectId) {
  fetch(`{{CONTEXT}}/rest/projects/id/${projectId}/trigger-build/release`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status} ${response.statusText}`);
      }
      alert("release build scheduled");
    })
    .catch((e) => {
      console.error(e);
      alert("could not schedule release build");
    });
}

function renderLastLogs() {
  const container = document.getElementById("outLastRuns");

  runs.reverse().forEach((log) => {
    const logLine = document.createElement("div");
    logLine.classList.add("logLine");
    const success = log["success"];

    log["steps"].forEach((step, index) => {
      const element = document.createElement("div");
      element.classList.add("step");
      element.innerText = step["step"];
      element.addEventListener("click", () => {
        openPopupWithLogs(step["log"]);
      });

      if (!success && index >= log["steps"].length - 1) {
        element.classList.add("fail");
      } else {
        element.classList.add("success");
      }
      logLine.appendChild(element);
    });

    const bttnRawLogs = document.createElement("button");
    bttnRawLogs.innerText = "raw";
    bttnRawLogs.addEventListener("click", () =>
      openPopupWithLogs(log["rawOutput"])
    );
    logLine.appendChild(bttnRawLogs);

    container.appendChild(logLine);
  });
}

function openPopupWithLogs(logs) {
  const container = document.createElement("div");

  logs.forEach((line) => {
    const p = document.createElement("p");
    p.innerText = line;
    container.appendChild(p);
  });

  openPopup(container);
}

function openSettings(id) {
  const content = document.createElement("div");

  const bttnSave = document.createElement("button");
  bttnSave.innerText = "save";
  bttnSave.addEventListener("click", () => saveUsedCredentials(id, bttnSave));
  content.appendChild(bttnSave);

  const headCredentials = document.createElement("h1");
  headCredentials.innerText = "credentials";
  content.appendChild(headCredentials);

  const ulCreds = document.createElement("ul");
  ulCreds.id = "outCredentials";
  content.appendChild(ulCreds);

  addWebhookInfo(id, content);

  openPopup(content);

  loadCredentials(data.credentials);
}

function addWebhookInfo(id, parent) {
  const headline = document.createElement("h1");
  headline.innerText = "webhooks";
  parent.appendChild(headline);

  ["github"].forEach((host) => {
    const bttn = document.createElement("button");
    bttn.innerText = host;
    bttn.addEventListener("click", () => {
      const url = `${window.location.protocol}//${window.location.host}/rest/projects/id/${id}/webhook/${host}`;
      navigator.clipboard.writeText(url).then(
        () => {
          cssPulse(true, bttn);
        },
        (err) => {
          console.error(err);
          cssPulse(false, bttn);
        }
      );
    });
    parent.appendChild(bttn);
  });
}

function cssPulse(success, element) {
  element.classList.add(`pulse${success ? "Green" : "Red"}`);
  setTimeout(() => {
    element.classList.remove(`pulse${success ? "Green" : "Red"}`);
  }, 750);
}

function loadCredentials(currentCredentials) {
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

        const cbUsed = document.createElement("input");
        cbUsed.type = "checkbox";
        cbUsed.checked = currentCredentials.includes(key);
        cbUsed.value = key;
        cbUsed.classList.add("cbCredentials");

        li.appendChild(cbUsed);

        container.appendChild(li);
      });
    })
    .catch((e) => {
      console.error(e);
      container.innerText = "could not load credentials";
    });
}

function saveUsedCredentials(id, self) {
  const usedCredentials = [];
  Array.from(document.getElementsByClassName("cbCredentials")).forEach(
    (element) => {
      if (element.checked) {
        usedCredentials.push(element.value);
      }
    }
  );

  fetch(`{{CONTEXT}}/rest/projects/id/${id}/credentials`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      credentials: usedCredentials,
    }),
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status} ${response.statusText}`);
      }
      closePopup(self);
    })
    .catch((e) => {
      console.error(e);
      alert("failed to save settings");
      closePopup(self);
    });
}
