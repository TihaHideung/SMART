<?php
header('Content-Type: application/json');
include "koneksi.php";

$query = $_GET['query'] ?? '';
$user_id = $_GET['user_id'] ?? '';

$response = [];

if (!empty($query) && !empty($user_id)) {
    $stmt = $conn->prepare("SELECT * FROM tasks WHERE id_user = ? AND title LIKE ?");
    $likeQuery = "%$query%";
    $stmt->bind_param("is", $user_id, $likeQuery);
    $stmt->execute();
    $result = $stmt->get_result();

    $tasks = [];
    while ($row = $result->fetch_assoc()) {
        $tasks[] = [
            "id_task" => (int)$row['id_task'],
            "title" => $row['title'],
            "description" => $row['description'],
            "due_date" => $row['due_date'],
            "due_time" => $row['due_time']
        ];
    }

    $response['status'] = 'success';
    $response['tasks'] = $tasks;
    $stmt->close();
} else {
    $response['status'] = 'error';
    $response['message'] = 'Parameter query atau user_id kosong';
}

echo json_encode($response);
