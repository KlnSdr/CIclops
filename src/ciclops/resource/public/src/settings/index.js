function loadSettings() {
  const gitName = document.getElementById("inputGitUsername");
  const gitMail = document.getElementById("inputGitMail");

  fetch(`{{CONTEXT}}/rest/settings`, {
    headers: {
      "Content-Type": "application/json",
    },
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status} ${response.statusText}`);
      }
      return response.json();
    })
    .then((data) => {
      gitName.value = data["gitUsername"];
      gitMail.value = data["gitMail"];
    })
    .catch((e) => {
      console.error(e);
      alert("failed to load settings");
    });
}

function updateSettings() {
  const gitName = document.getElementById("inputGitUsername").value;
  const gitMail = document.getElementById("inputGitMail").value;
  const button = document.getElementById("updateSettings");

  button.innerText = "speichern...";

  fetch(`{{CONTEXT}}/rest/settings`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      gitUsername: gitName,
      gitMail: gitMail,
    }),
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status} ${response.statusText}`);
      }
      loadSettings();

      button.innerText = "speichern";
    })
    .catch((e) => {
      console.error(e);
      alert("failed to update settings");
      button.innerText = "speichern";
    });
}

function loadBuilderImage() {
  const builderImage = document.getElementById("inputBuilderImage");

  fetch(`{{CONTEXT}}/rest/builderimage`, {
    headers: {
      "Content-Type": "application/json",
    },
  })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP ${response.status} ${response.statusText}`);
        }
        return response.json();
      })
      .then((data) => {
        builderImage.value = data["builderImage"];
      })
      .catch((e) => {
        console.error(e);
        alert("failed to load builder image");
      });
}

function updateBuilderImage() {
  const builderImage = document.getElementById("inputBuilderImage").value;
  const button = document.getElementById("updateBuilderImage");

  button.innerText = "speichern...";

  fetch(`{{CONTEXT}}/rest/builderImage`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      builderImage: builderImage
    }),
  })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP ${response.status} ${response.statusText}`);
        }
        loadSettings();

        button.innerText = "speichern";
      })
      .catch((e) => {
        console.error(e);
        alert("failed to update builder image");
        button.innerText = "speichern";
      });
}
