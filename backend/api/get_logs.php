<?php
// Tidak boleh ada spasi, newline, atau karakter sebelum tag ini
ob_clean();

header('Content-Type: application/json');
require_once 'koneksi.php';

$response = ['status' => 'error', 'logs' => []];

if (isset($_GET['id_user'])) {
    $id_user = intval($_GET['id_user']);

    $sql = "SELECT * FROM loguser WHERE id_user = ? ORDER BY timestamp DESC";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $id_user);
    $stmt->execute();
    $result = $stmt->get_result();

    $logs = [];
    while ($row = $result->fetch_assoc()) {
        $logs[] = [
            'id_log' => $row['id_log'],
            'activity' => $row['activity'],
            'timestamp' => $row['timestamp']
        ];
    }

    $response['status'] = 'success';
    $response['logs'] = $logs;
}

echo json_encode($response);
exit;
