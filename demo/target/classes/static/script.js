const commentButton = document.getElementById('commentButton');

commentButton.addEventListener('click', () => {
  // Replace with your actual Spring Boot project URL and port
  const baseUrl = "http://localhost:8080";
  const postId = 1; // Replace with the actual post ID

  const commentUrl = `${baseUrl}/post/${postId}/comment`;

  // Simulate a GET request (Modify for your actual comment submission logic)
  fetch(commentUrl, {
    method: 'GET' // Replace with 'POST' for actual comment submission
  })
  .then(response => {
    if (response.ok) {
      alert("Your comment has been submitted (simulated)");
    } else {
      alert("Error submitting comment (simulated)");
    }
  })
  .catch(error => {
    console.error('Error:', error);
  });
});
