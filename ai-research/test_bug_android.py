"""
Test rapide : démontrer le bug Android [0,1] vs [0,255].
"""
import json
import os
import numpy as np
from PIL import Image

try:
    import tflite_runtime.interpreter as tflite
except ImportError:
    import tensorflow.lite as tflite

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "AgriPredict_resultsV2", "AgriPredict_resultsV2",
                          "models", "mobilenetv2_agri_int8.tflite")
MODEL_F32_PATH = os.path.join(BASE_DIR, "AgriPredict_resultsV2", "AgriPredict_resultsV2",
                              "models", "mobilenetv2_agri_float32.tflite")
LABELS_PATH = os.path.join(BASE_DIR, "AgriPredict_resultsV2", "AgriPredict_resultsV2",
                           "models", "class_names.json")
DATA_DIR = os.path.join(BASE_DIR, "data")
IMG_SIZE = 224

with open(LABELS_PATH) as f:
    labels = json.load(f)

# Charger les modèles
interp_int8 = tflite.Interpreter(model_path=MODEL_PATH)
interp_int8.allocate_tensors()

interp_f32 = tflite.Interpreter(model_path=MODEL_F32_PATH)
interp_f32.allocate_tensors()


def run(interpreter, input_data):
    inp = interpreter.get_input_details()[0]
    out = interpreter.get_output_details()[0]
    input_data = input_data.astype(inp['dtype'])
    interpreter.set_tensor(inp['index'], input_data)
    interpreter.invoke()
    return interpreter.get_tensor(out['index'])[0]


# Images de test
test_classes = [
    "Tomato___Late_blight",
    "Corn_(maize)___Common_rust_",
    "Potato___Early_blight",
    "Pepper,_bell___Bacterial_spot",
    "cassava___mosaic_disease_(cmd)",
]

print("\n" + "=" * 100)
print("🔬 COMPARAISON : Preprocessing [0,255] (correct) vs [0,1] (bug Android)")
print("=" * 100)
print(f"\n{'Vraie classe':<40} {'[0,255] Pred':<25} {'Conf':>6}   {'[0,1] Pred':<25} {'Conf':>6}   {'F32 [0,255] Pred':<25} {'Conf':>6}")
print("-" * 140)

for class_name in test_classes:
    class_dir = os.path.join(DATA_DIR, class_name)
    if not os.path.isdir(class_dir):
        continue
    images = [f for f in os.listdir(class_dir)
              if f.lower().endswith(('.jpg', '.jpeg', '.png', '.bmp'))]
    if not images:
        continue

    img = Image.open(os.path.join(class_dir, images[0])).convert("RGB")
    img = img.resize((IMG_SIZE, IMG_SIZE), Image.BILINEAR)
    arr = np.array(img, dtype=np.float32)

    # Preprocessing CORRECT : [0, 255] — ce que le modèle attend
    input_correct = np.expand_dims(arr, axis=0)

    # Preprocessing BUG ANDROID : [0, 1] — ce que le Kotlin fait actuellement
    input_buggy = np.expand_dims(arr / 255.0, axis=0)

    # INT8 avec [0, 255]
    p1 = run(interp_int8, input_correct)
    idx1 = np.argmax(p1)
    ok1 = "✅" if labels[idx1] == class_name else "❌"

    # INT8 avec [0, 1] (BUG)
    p2 = run(interp_int8, input_buggy)
    idx2 = np.argmax(p2)
    ok2 = "✅" if labels[idx2] == class_name else "❌"

    # FLOAT32 avec [0, 255]
    p3 = run(interp_f32, input_correct)
    idx3 = np.argmax(p3)
    ok3 = "✅" if labels[idx3] == class_name else "❌"

    print(f"{class_name:<40} "
          f"{ok1} {labels[idx1][:20]:<22} {p1[idx1]:5.1%}   "
          f"{ok2} {labels[idx2][:20]:<22} {p2[idx2]:5.1%}   "
          f"{ok3} {labels[idx3][:20]:<22} {p3[idx3]:5.1%}")

print()

# Extra : montrer les top-5 prédictions avec le bug
print("\n" + "=" * 100)
print("🔎 DÉTAIL : Top-5 prédictions sur Tomato___Late_blight")
print("=" * 100)

test_img_path = os.path.join(DATA_DIR, "Tomato___Late_blight")
imgs = os.listdir(test_img_path)
img = Image.open(os.path.join(test_img_path, imgs[0])).convert("RGB")
img = img.resize((IMG_SIZE, IMG_SIZE), Image.BILINEAR)
arr = np.array(img, dtype=np.float32)

print("\n📊 Avec [0, 255] (CORRECT) :")
p = run(interp_int8, np.expand_dims(arr, axis=0))
top5 = np.argsort(p)[::-1][:5]
for i, idx in enumerate(top5):
    print(f"  {i+1}. {labels[idx]:<50s} {p[idx]:6.2%}")

print("\n📊 Avec [0, 1] (BUG ANDROID — division par 255) :")
p_bug = run(interp_int8, np.expand_dims(arr / 255.0, axis=0))
top5_bug = np.argsort(p_bug)[::-1][:5]
for i, idx in enumerate(top5_bug):
    print(f"  {i+1}. {labels[idx]:<50s} {p_bug[idx]:6.2%}")

print("\n📊 Avec FLOAT32 [0, 255] (RÉFÉRENCE) :")
p_f32 = run(interp_f32, np.expand_dims(arr, axis=0))
top5_f32 = np.argsort(p_f32)[::-1][:5]
for i, idx in enumerate(top5_f32):
    print(f"  {i+1}. {labels[idx]:<50s} {p_f32[idx]:6.2%}")

# Montrer la dégradation
print(f"\n{'='*100}")
print("💡 CONCLUSION")
print(f"{'='*100}")
print(f"  Le modèle attend des pixels en [0, 255] (float32).")
print(f"  La couche Rescaling(1/127.5, -1) INTERNE au modèle convertit vers [-1, 1].")
print(f"  Le code Android divise par 255 → envoie [0, 1] → le modèle les convertit en [-1, -0.992]")
print(f"  → Toutes les valeurs sont proches de -1 → image quasi noire → prédiction aléatoire.")
print(f"\n  FIX : Ne PAS diviser par 255 dans bitmapToByteBufferFloat32().")
print(f"  Envoyer les pixels bruts (0-255) en float32.")
