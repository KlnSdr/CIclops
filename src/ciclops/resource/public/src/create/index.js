function createProject() {
  const name = document.getElementById("inputName").value;
  const scm = document.getElementById("inputSCM").value;

  fetch(`{{CONTEXT}}/rest/projects`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      name: name,
      gitUrl: scm,
    }),
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status} ${response.statusText}`);
      }
      return response.json();
    })
    .then((project) => {
      console.log(project);
    })
    .catch((e) => {
      console.error(e);
      alert("Could not create project");
    });
}
