const likePost = async (objectData) => {
    try {
        const response = await axios.post('/posts/like', objectData);
        console.log('Object saved successfully:', response.data);
        return response.data;
    } catch (error) {
        console.error('Error saving object:', error);
        throw error;
    }
};

const unlikePost = async (objectId) => {
    try {
        const response = await axios.post(`/posts/unlike/${objectId}`);
        console.log('Object deleted successfully:', response.data);
        return response.data;
    } catch (error) {
        console.error('Error deleting object:', error);
        throw error;
    }
};

document.addEventListener('click', async function(event) {
    if (event.target.classList.contains('like')) {
        const buttonId = event.target.id;
        const index = parseInt(buttonId.split('-')[1]);
        console.log('Button clicked. Index:', index);

        // Retrieve the post object using the index
        const post = posts[index];
        const likeList = post.likeList;

        // Perform logic to update the like list
        if (likeList) {
            post.likeList.push(userId);
            console.log('Updated post:', post);
        } else {
            console.error('Post or likeList is undefined.');
        }

        try {
            await likePost(post);
            console.log('Post liked successfully:', post);
        } catch (error) {
            console.error('Error liking post:', error);
        }
        document.getElementById(buttonId).className = "disL fa-solid fa-heart";
        var likes = parseInt(document.getElementById('likes-' + index).textContent);
        likes = likes + 1;
        document.getElementById('likes-' + index).textContent = likes + '';
    }
});

document.addEventListener('click', async function(event) {
    if(event.target.classList.contains('disL')) {
        const buttonId = event.target.id;
        const index = parseInt(buttonId.split('-')[1]);
        console.log('Button clicked. Index:', index);

        const post = posts[index];
        const likeList = post.likeList;

        if (likeList) {
            console.log('Updated post:', post);
        } else {
            console.error('Post or likeList is undefined.');
        }

        try {
             delete post.likeList[post.likeList.indexOf(userId)];
             await unlikePost(post.id);
             console.log('Post liked successfully:', post);
        } catch (error) {
             console.error('Error liking post:', error);
        }
        document.getElementById(buttonId).className = "like fa-regular fa-heart";
        var likes = parseInt(document.getElementById('likes-' + index).textContent);
        likes = likes - 1;
        document.getElementById('likes-' + index).textContent = likes + '';
    }
});