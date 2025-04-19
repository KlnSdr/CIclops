function init() {
  loadProjects();
  addTaskGetRunners();
}

function loadProjects() {
  const container = document.getElementById("output");
  fetch("{{CONTEXT}}/rest/projects")
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status} ${response.statusText}`);
      }
      return response.json();
    })
    .then((data) => {
      const projects = data["projects"];
      const ul = document.createElement("ul");

      projects.forEach((project) => {
        const li = document.createElement("li");

        const link = document.createElement("a");
        link.href = "{{CONTEXT}}/projects/id/" + project["id"];
        link.innerText = project["name"];
        li.appendChild(link);

        ul.appendChild(li);
      });
      container.appendChild(ul);
    })
    .catch((e) => {
      console.error(e);
      container.innerText = "could not load projects";
    });
}

function addTaskGetRunners() {
  setInterval(() => {
    const container = document.getElementById("outRunningJobs");
    fetch("{{CONTEXT}}/rest/runners")
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP ${response.status} ${response.statusText}`);
        }
        return response.json();
      })
      .then((data) => {
        container.innerHTML = "";
        const runners = data["runningJobs"];
        const ul = document.createElement("ul");

        runners.forEach((runner) => {
          const li = document.createElement("li");

          const link = document.createElement("a");
          link.innerText = runner;
          li.appendChild(link);

          ul.appendChild(li);
        });
        container.appendChild(ul);
      })
      .catch((e) => {
        console.error(e);
        container.innerText = "could not load running jobs";
      });
  }, 5000);
}
