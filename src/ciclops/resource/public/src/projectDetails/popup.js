function openPopup(contentBody) {
  const background = document.createElement("div");
  background.setAttribute("X-Type", "popupBackground");
  background.classList.add("popupBackground");
  background.onclick = (event) => {
    if (event.target === background) {
      closePopup(background);
    }
  };

  const popup = document.createElement("div");
  popup.classList.add("popup");

  const close = document.createElement("button");
  close.appendChild(document.createTextNode("X"));
  close.onclick = () => background.remove();
  popup.appendChild(close);

  const content = document.createElement("div");
  content.appendChild(contentBody);
  popup.appendChild(content);

  background.appendChild(popup);
  document.body.appendChild(background);
}

function closePopup(self) {
  if (self === document.body || self === null) {
    return;
  }

  if (self.getAttribute("X-Type") === "popupBackground") {
    self.remove();
  } else {
    closePopup(self.parentElement);
  }
}
