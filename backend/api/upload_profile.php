<?php
// Aktifkan error reporting (penting untuk debug saat pengembangan)
ini_set('display_errors', 1);
error_reporting(E_ALL);

// Set header agar browser/client tahu ini response JSON
header('Content-Type: application/json');

require_once 'koneksi.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // Cek apakah data yang dibutuhkan tersedia
    if (isset($_POST['user_id'], $_POST['name'])) {
        $user_id = $_POST['user_id'];
        $name = $_POST['name'];

        // Cek apakah file gambar dikirim
        if (isset($_FILES['image'])) {
            $image_name = time() . "_" . basename($_FILES['image']['name']);
            $target_path = "uploads/" . $image_name;

            // Pastikan direktori uploads tersedia
            if (!is_dir("uploads")) {
                mkdir("uploads", 0755, true);
            }

            // Coba pindahkan file yang diupload
            if (move_uploaded_file($_FILES['image']['tmp_name'], $target_path)) {
                // Update database
                $stmt = $conn->prepare("UPDATE users SET name = ?, profile_picture = ? WHERE id_user = ?");
                $stmt->bind_param("ssi", $name, $target_path, $user_id);

                if ($stmt->execute()) {
                    echo json_encode([
                        "status" => "success",
                        "message" => "Profil disimpan",
                        "profile_picture" => $target_path
                    ]);
                } else {
                    echo json_encode([
                        "status" => "error",
                        "message" => "Gagal menyimpan ke database"
                    ]);
                }

                $stmt->close();
            } else {
                echo json_encode([
                    "status" => "error",
                    "message" => "Gagal mengunggah gambar"
                ]);
            }
        } else {
            echo json_encode([
                "status" => "error",
                "message" => "File gambar tidak ditemukan"
            ]);
        }
    } else {
        echo json_encode([
            "status" => "error",
            "message" => "Parameter tidak lengkap"
        ]);
    }
} else {
    echo json_encode([
        "status" => "error",
        "message" => "Metode tidak diizinkan"
    ]);
}
