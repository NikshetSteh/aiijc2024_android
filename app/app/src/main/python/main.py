# # import cv2
# # import numpy as np
# # import os
# # from os.path import dirname, join
# #
# # def preprocess(image_path: str):
# #     img = cv2.imread(image_path)
# #     gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
# #     clahe = cv2.createCLAHE(clipLimit=2.5, tileGridSize=(8, 8))
# #     gray = clahe.apply(gray)
# #     blur = cv2.GaussianBlur(gray, (9, 9), 2)
# #     kernel = np.ones((5, 5), np.uint8)
# #     morph = cv2.morphologyEx(blur, cv2.MORPH_CLOSE, kernel)
# #
# #     edges = cv2.Canny(morph, 50, 150)
# #
# #     circles = cv2.HoughCircles(morph, cv2.HOUGH_GRADIENT, dp=1.2, minDist=20, param1=100, param2=40, minRadius=20,
# #                                maxRadius=50)
# #
# #     mask = np.zeros_like(gray)
# #     if circles is not None:
# #         circles = np.round(circles[0, :]).astype("int")
# #         for (x, y, r) in circles:
# #             cv2.circle(mask, (x, y), r, 255, -1)
# #
# #     num_tubes = len(circles) if circles is not None else 0
# #
# #     result = cv2.bitwise_and(img, img, mask=mask)
# #
# #     return result, num_tubes, mask
# #
# #
# # def main(image_path: str):
# #     # os.path.getsize(join(dirname(__file__), "test_file"))
# #
# #     if image_path.startswith("file://"):
# #         image_path = image_path[7:]
# #
# #     blur = preprocess(image_path)
# #     processed_image, num_tubes, mask = preprocess(image_path)
# #
# #     cv2.imwrite(image_path, processed_image)
# #
# #     return num_tubes
#
#
# from yolov5 import detect
# import cv2
# import os
# from yolov5.utils.plots import save_one_box
# from os.path import dirname, join
#
#
# def detect_and_count_pipes(weights, source, imgsz):
#     # Приведение изображений к определенному размеру
#
#     # Выполнение предсказаний
#     results = detect.run(weights=weights, source=dirname(source), imgsz=imgsz, save_txt=False, save_conf=False)
#
#     # Получение списка файлов изображений
#     img_files = [source]
#
#     # Подсчет труб и обрамление их в тонкую рамку
#     for img_path in img_files:
#         img = cv2.imread(img_path)
#         img = cv2.resize(img, (imgsz, imgsz))
#
#         txt_path = img_path.replace('.jpg', '.txt').replace('.JPG', '.txt')
#         if os.path.exists(txt_path):
#             with open(txt_path, 'r', encoding='utf-8') as f:
#                 lines = f.readlines()
#                 pipe_count = 0
#                 for line in lines:
#                     parts = line.strip().split()
#                     conf = float(parts[5])
#                     if conf >= 0.5:
#                         pipe_count += 1
#                         x, y, w, h = map(float, parts[1:5])
#                         save_one_box([x, y, x + w, y + h], img, color=(0, 255, 0), line_thickness=1)
#             cv2.imwrite(img_path, img)
#             return pipe_count
#             # cv2.putText(img, f'Pipe count: {pipe_count}', (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)
#         else:
#             print(f'No predictions found for {img_path}')
#
#
# def main(image_path):
#     weights = os.path.getsize(join(dirname(__file__), "model.pt"))
#     imgsz = 640
#
#     if image_path.startswith("file://"):
#         image_path = image_path[7:]
#
#     return detect_and_count_pipes(weights, image_path, imgsz)


import os
from yolov5.models.experimental import attempt_load
from yolov5.utils.general import non_max_suppression, scale_boxes
# from yolov5.utils.torch_utils import select_device
from os.path import dirname, join
import cv2
import torch


def is_inside(inner, outer):
    """Проверяет, находится ли рамка inner внутри рамки outer"""
    return inner[0] > outer[0] and inner[1] > outer[1] and inner[2] < outer[2] and inner[3] < outer[3]


def detect_and_count_pipes(weights, img_path, imgsz):
    # resize_image(img_path, imgsz)

    # Загрузка модели
    model = attempt_load(weights)
    model.eval()

    # Подсчет труб
    img = cv2.imread(img_path)
    img_resized = cv2.resize(img, (imgsz, imgsz))
    img_tensor = torch.from_numpy(img_resized).permute(2, 0, 1).float().unsqueeze(0) / 255.0
    img_tensor = img_tensor.cpu()

    with torch.no_grad():
        pred = model(img_tensor)[0]
        pred = non_max_suppression(pred, 0.6, 0.45)

    boxes = []
    for det in pred:
        if len(det):
            det[:, :4] = scale_boxes(img_tensor.shape[2:], det[:, :4], img.shape).round()
            for *xyxy, conf, cls in det:
                boxes.append((xyxy, conf))

    # Удаление рамок, которые находятся внутри других рамок
    filtered_boxes = []
    for i, (inner, conf) in enumerate(boxes):
        if not any(is_inside(inner, outer) for j, (outer, _) in enumerate(boxes) if i != j):
            filtered_boxes.append((inner, conf))

    pipe_count = len(filtered_boxes)
    for xyxy, conf in filtered_boxes:
        x1, y1, x2, y2 = map(int, xyxy)
        cv2.rectangle(img, (x1, y1), (x2, y2), (0, 255, 0), 1)

    annot_path = os.path.basename(img_path).replace('.JPG', '.jpg').replace('.jpeg', '.jpg')

    cv2.imwrite(img_path, img)
    return pipe_count


def main(image_path):
    if image_path.startswith("file://"):
        image_path = image_path[7:]
    # Путь к модели
    weights = join(dirname(__file__), "model.pt")
    # Размер изображения
    imgsz = 640

    return detect_and_count_pipes(weights, image_path, imgsz)