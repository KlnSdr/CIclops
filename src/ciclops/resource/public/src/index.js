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
        li.innerText = project["name"];
        ul.appendChild(li);
      });
      container.appendChild(ul);
    })
    .catch((e) => {
      console.error(e);
      container.innerText = "could not load projects";
    });
}
