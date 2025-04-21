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
