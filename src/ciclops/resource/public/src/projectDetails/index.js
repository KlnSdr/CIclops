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
