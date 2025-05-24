document.querySelectorAll('.action-buttons a:not(.delete)').forEach(editBtn => {
  editBtn.addEventListener('click', function (e) {
    e.stopPropagation();

    const row = this.closest('tr');
    const titleCell = row.children[0];
    const timeCell = row.children[1];

    if (titleCell.querySelector('input')) return;

    const originalTitle = titleCell.textContent;
    const originalTime = timeCell.textContent;

    const titleInput = document.createElement('input');
    titleInput.type = 'text';
    titleInput.value = originalTitle;
    titleInput.style.width = '100%';

    const timeInput = document.createElement('input');
    timeInput.type = 'text';
    timeInput.value = originalTime;
    timeInput.style.width = '100%';

    titleCell.textContent = '';
    timeCell.textContent = '';
    titleCell.appendChild(titleInput);
    timeCell.appendChild(timeInput);
    titleInput.focus();

    function saveOnEnter(e) {
      if (e.key === 'Enter') {
        titleCell.textContent = titleInput.value;
        timeCell.textContent = timeInput.value;
        removeClickListener();
      }
    }

    titleInput.addEventListener('keydown', saveOnEnter);
    timeInput.addEventListener('keydown', saveOnEnter);

    function handleOutsideClick(ev) {
      if (!row.contains(ev.target)) {
        titleCell.textContent = originalTitle;
        timeCell.textContent = originalTime;
        removeClickListener();
      }
    }

    function removeClickListener() {
      document.removeEventListener('click', handleOutsideClick);
    }

    setTimeout(() => {
      document.addEventListener('click', handleOutsideClick);
    }, 0);
  });
}); 
