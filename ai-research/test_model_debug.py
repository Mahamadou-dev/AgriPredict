"""
Script de diagnostic : Comparaison des prédictions du modèle INT8.

Teste le modèle TFLite INT8 avec :
  1) Le preprocessing CORRECT (tel que fait pendant l'entraînement)
  2) Le preprocessing ANDROID (tel que fait dans TFLiteClassifier.kt)

Objectif : identifier si le problème vient du modèle ou du preprocessing Android.
"""

import json
import os
import sys
import numpy as np
from PIL import Image

# ==============================================================================
# Essayer tflite_runtime d'abord, sinon fallback sur tensorflow.lite
# ==============================================================================
try:
    import tflite_runtime.interpreter as tflite
    print("✅ Utilisation de tflite_runtime")
except ImportError:
    import tensorflow.lite as tflite
    print("✅ Utilisation de tensorflow.lite")


# ==============================================================================
# CONFIGURATION
# ==============================================================================
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# Modèle INT8 (celui copié dans les assets Android)
MODEL_PATH = os.path.join(BASE_DIR, "AgriPredict_resultsV2", "AgriPredict_resultsV2",
                          "models", "mobilenetv2_agri_int8.tflite")

# Modèle FLOAT32 (pour comparaison)
MODEL_FLOAT32_PATH = os.path.join(BASE_DIR, "AgriPredict_resultsV2", "AgriPredict_resultsV2",
                                  "models", "mobilenetv2_agri_float32.tflite")

# Labels
LABELS_PATH = os.path.join(BASE_DIR, "AgriPredict_resultsV2", "AgriPredict_resultsV2",
                           "models", "class_names.json")

# Images de test (une par classe pour validation)
DATA_DIR = os.path.join(BASE_DIR, "data")

IMG_SIZE = 224

# ==============================================================================
# CHARGEMENT
# ==============================================================================

def load_labels():
    with open(LABELS_PATH, "r") as f:
        return json.load(f)


def load_interpreter(model_path):
    interpreter = tflite.Interpreter(model_path=model_path)
    interpreter.allocate_tensors()
    return interpreter


def print_model_info(interpreter, name="Modèle"):
    """Affiche les détails des tenseurs d'entrée et de sortie."""
    input_details = interpreter.get_input_details()[0]
    output_details = interpreter.get_output_details()[0]

    print(f"\n{'='*70}")
    print(f"📋 {name}")
    print(f"{'='*70}")
    print(f"  ENTRÉE:")
    print(f"    Shape       : {input_details['shape']}")
    print(f"    Dtype       : {input_details['dtype']}")
    print(f"    Quant params: {input_details.get('quantization_parameters', 'N/A')}")
    if 'quantization' in input_details:
        print(f"    Quantization: {input_details['quantization']}")

    print(f"  SORTIE:")
    print(f"    Shape       : {output_details['shape']}")
    print(f"    Dtype       : {output_details['dtype']}")
    print(f"    Quant params: {output_details.get('quantization_parameters', 'N/A')}")
    if 'quantization' in output_details:
        print(f"    Quantization: {output_details['quantization']}")
    print()


# ==============================================================================
# PREPROCESSING #1 : CORRECT (comme durant l'entraînement)
# Le modèle d'inférence inclut Rescaling(1/127.5, -1), donc il attend [0, 255].
# ==============================================================================

def preprocess_correct(image_path):
    """
    Preprocessing identique au pipeline d'entraînement :
    - Charger l'image avec PIL (RGB)
    - Redimensionner à 224x224 (bilinéaire)
    - Convertir en float32 dans [0, 255]
    Le modèle se charge de la normalisation [-1, 1] via sa couche Rescaling interne.
    """
    img = Image.open(image_path).convert("RGB")
    img = img.resize((IMG_SIZE, IMG_SIZE), Image.BILINEAR)
    arr = np.array(img, dtype=np.float32)  # [0, 255] float32
    arr = np.expand_dims(arr, axis=0)       # (1, 224, 224, 3)
    return arr


# ==============================================================================
# PREPROCESSING #2 : ANDROID (simule bitmapToByteBufferUint8 du Kotlin)
# ==============================================================================

def preprocess_android_uint8(image_path):
    """
    Simule exactement le code Android bitmapToByteBufferUint8() :
    - Charger l'image
    - Redimensionner à 224x224
    - Extraire les pixels bruts R,G,B en UINT8 [0, 255]
    - Pas de normalisation (valeurs brutes 0-255 en uint8)
    """
    img = Image.open(image_path).convert("RGB")
    img = img.resize((IMG_SIZE, IMG_SIZE), Image.BILINEAR)
    arr = np.array(img, dtype=np.uint8)    # [0, 255] uint8
    arr = np.expand_dims(arr, axis=0)       # (1, 224, 224, 3)
    return arr


def preprocess_android_float32(image_path):
    """
    Simule exactement le code Android bitmapToByteBufferFloat32() :
    - Charger l'image
    - Redimensionner à 224x224
    - Normaliser les pixels R,G,B en FLOAT32 [0.0, 1.0] (division par 255)
    
    ATTENTION : Ceci est INCORRECT pour MobileNetV2 qui attend [-1, 1] via Rescaling.
    Le /255.0 donne [0, 1] au lieu de [0, 255] en entrée du Rescaling.
    """
    img = Image.open(image_path).convert("RGB")
    img = img.resize((IMG_SIZE, IMG_SIZE), Image.BILINEAR)
    arr = np.array(img, dtype=np.float32) / 255.0  # [0, 1] float32
    arr = np.expand_dims(arr, axis=0)
    return arr


# ==============================================================================
# INFÉRENCE
# ==============================================================================

def run_inference(interpreter, input_data):
    """Exécute l'inférence et retourne les probabilités."""
    input_details = interpreter.get_input_details()[0]
    output_details = interpreter.get_output_details()[0]

    # Adapter le type de l'entrée si nécessaire
    expected_dtype = input_details['dtype']
    if input_data.dtype != expected_dtype:
        if expected_dtype == np.uint8:
            # Quantifier l'entrée float → uint8 avec les params du modèle
            quant_params = input_details.get('quantization_parameters', {})
            scales = quant_params.get('scales', np.array([1.0]))
            zero_points = quant_params.get('zero_points', np.array([0]))
            if len(scales) > 0 and scales[0] != 0:
                input_data = (input_data / scales[0] + zero_points[0])
                input_data = np.clip(input_data, 0, 255).astype(np.uint8)
            else:
                input_data = input_data.astype(np.uint8)
        elif expected_dtype == np.float32:
            input_data = input_data.astype(np.float32)

    interpreter.set_tensor(input_details['index'], input_data)
    interpreter.invoke()
    output = interpreter.get_tensor(output_details['index'])[0]

    # Dé-quantifier la sortie si nécessaire
    if output_details['dtype'] == np.uint8:
        quant_params = output_details.get('quantization_parameters', {})
        scales = quant_params.get('scales', np.array([1.0]))
        zero_points = quant_params.get('zero_points', np.array([0]))
        if len(scales) > 0 and scales[0] != 0:
            output = (output.astype(np.float32) - zero_points[0]) * scales[0]
        else:
            output = output.astype(np.float32) / 255.0

    return output


# ==============================================================================
# TEST PRINCIPAL
# ==============================================================================

def find_test_images(data_dir, labels, n_per_class=1):
    """Trouve une image par classe dans le dataset."""
    test_images = []
    for label in labels:
        class_dir = os.path.join(data_dir, label)
        if os.path.isdir(class_dir):
            images = [f for f in os.listdir(class_dir)
                      if f.lower().endswith(('.jpg', '.jpeg', '.png', '.bmp'))]
            if images:
                test_images.append({
                    'path': os.path.join(class_dir, images[0]),
                    'true_label': label,
                    'true_index': labels.index(label)
                })
    return test_images


def main():
    print("🔍 DIAGNOSTIC DU MODÈLE INT8 — AgriPredict")
    print("=" * 70)

    # Charger labels
    labels = load_labels()
    print(f"📋 {len(labels)} classes chargées")

    # Charger les modèles
    if not os.path.exists(MODEL_PATH):
        print(f"❌ Modèle INT8 introuvable : {MODEL_PATH}")
        sys.exit(1)

    interp_int8 = load_interpreter(MODEL_PATH)
    print_model_info(interp_int8, "Modèle INT8")

    interp_float32 = None
    if os.path.exists(MODEL_FLOAT32_PATH):
        interp_float32 = load_interpreter(MODEL_FLOAT32_PATH)
        print_model_info(interp_float32, "Modèle FLOAT32 (référence)")

    # Trouver des images de test
    test_images = find_test_images(DATA_DIR, labels, n_per_class=1)
    print(f"\n🖼️  {len(test_images)} images de test trouvées\n")

    if not test_images:
        print("❌ Aucune image de test trouvée!")
        sys.exit(1)

    # =====================================================
    # TEST : Comparer les prédictions
    # =====================================================

    results = []
    n_correct_int8_correct_preproc = 0
    n_correct_int8_android_preproc = 0
    n_correct_float32 = 0
    n_total = 0

    print(f"\n{'='*120}")
    print(f"{'Image':<55} {'Vraie classe':<35} {'INT8 correct':<25} {'INT8 android':<25} {'FLOAT32':<25}")
    print(f"{'='*120}")

    for img_info in test_images:
        image_path = img_info['path']
        true_label = img_info['true_label']
        true_idx = img_info['true_index']
        n_total += 1

        # --- Test 1 : INT8 avec preprocessing CORRECT (float32 [0,255]) ---
        input_correct = preprocess_correct(image_path)
        probs_correct = run_inference(interp_int8, input_correct)
        pred_correct_idx = np.argmax(probs_correct)
        pred_correct_conf = probs_correct[pred_correct_idx]

        if pred_correct_idx == true_idx:
            n_correct_int8_correct_preproc += 1

        # --- Test 2 : INT8 avec preprocessing ANDROID (uint8 [0,255] brut) ---
        input_android = preprocess_android_uint8(image_path)
        probs_android = run_inference(interp_int8, input_android)
        pred_android_idx = np.argmax(probs_android)
        pred_android_conf = probs_android[pred_android_idx]

        if pred_android_idx == true_idx:
            n_correct_int8_android_preproc += 1

        # --- Test 3 : FLOAT32 (référence) ---
        float32_str = "N/A"
        if interp_float32:
            input_f32 = preprocess_correct(image_path)
            probs_f32 = run_inference(interp_float32, input_f32)
            pred_f32_idx = np.argmax(probs_f32)
            pred_f32_conf = probs_f32[pred_f32_idx]
            if pred_f32_idx == true_idx:
                n_correct_float32 += 1
            ok_f32 = "✅" if pred_f32_idx == true_idx else "❌"
            float32_str = f"{ok_f32} {labels[pred_f32_idx][:15]:15s} {pred_f32_conf:.1%}"

        ok1 = "✅" if pred_correct_idx == true_idx else "❌"
        ok2 = "✅" if pred_android_idx == true_idx else "❌"

        img_name = os.path.basename(image_path)[:50]
        print(f"{img_name:<55} {true_label[:33]:<35} "
              f"{ok1} {labels[pred_correct_idx][:15]:15s} {pred_correct_conf:5.1%}   "
              f"{ok2} {labels[pred_android_idx][:15]:15s} {pred_android_conf:5.1%}   "
              f"{float32_str}")

    # =====================================================
    # RÉSUMÉ
    # =====================================================
    print(f"\n{'='*70}")
    print(f"📊 RÉSUMÉ ({n_total} images)")
    print(f"{'='*70}")
    print(f"  INT8 + preprocessing correct (float32 [0,255]) : "
          f"{n_correct_int8_correct_preproc}/{n_total} = {n_correct_int8_correct_preproc/n_total:.1%}")
    print(f"  INT8 + preprocessing Android (uint8 brut)      : "
          f"{n_correct_int8_android_preproc}/{n_total} = {n_correct_int8_android_preproc/n_total:.1%}")
    if interp_float32:
        print(f"  FLOAT32 + preprocessing correct                : "
              f"{n_correct_float32}/{n_total} = {n_correct_float32/n_total:.1%}")

    # =====================================================
    # DIAGNOSTIC DÉTAILLÉ des quantization params
    # =====================================================
    print(f"\n{'='*70}")
    print("🔬 ANALYSE DES QUANTIZATION PARAMETERS (INT8)")
    print(f"{'='*70}")

    input_details = interp_int8.get_input_details()[0]
    output_details = interp_int8.get_output_details()[0]

    inp_quant = input_details.get('quantization_parameters', {})
    out_quant = output_details.get('quantization_parameters', {})

    inp_scale = inp_quant.get('scales', np.array([0]))[0] if len(inp_quant.get('scales', [])) > 0 else 0
    inp_zp = inp_quant.get('zero_points', np.array([0]))[0] if len(inp_quant.get('zero_points', [])) > 0 else 0
    out_scale = out_quant.get('scales', np.array([0]))[0] if len(out_quant.get('scales', [])) > 0 else 0
    out_zp = out_quant.get('zero_points', np.array([0]))[0] if len(out_quant.get('zero_points', [])) > 0 else 0

    print(f"\n  INPUT quantization:")
    print(f"    scale     = {inp_scale}")
    print(f"    zero_point= {inp_zp}")
    if inp_scale > 0:
        print(f"    → float_value = (uint8_value - {inp_zp}) * {inp_scale}")
        print(f"    → uint8=0   → float = {(0 - inp_zp) * inp_scale:.4f}")
        print(f"    → uint8=128 → float = {(128 - inp_zp) * inp_scale:.4f}")
        print(f"    → uint8=255 → float = {(255 - inp_zp) * inp_scale:.4f}")
        print(f"\n    Pour envoyer float=0.0   → uint8 = {int(0.0 / inp_scale + inp_zp)}")
        print(f"    Pour envoyer float=127.5 → uint8 = {int(127.5 / inp_scale + inp_zp)}")
        print(f"    Pour envoyer float=255.0 → uint8 = {int(255.0 / inp_scale + inp_zp)}")

    print(f"\n  OUTPUT quantization:")
    print(f"    scale     = {out_scale}")
    print(f"    zero_point= {out_zp}")
    if out_scale > 0:
        print(f"    → float_value = (uint8_value - {out_zp}) * {out_scale}")

    # DIAGNOSTIC
    print(f"\n{'='*70}")
    print("🔎 DIAGNOSTIC")
    print(f"{'='*70}")

    if n_correct_int8_correct_preproc > n_correct_int8_android_preproc:
        diff = n_correct_int8_correct_preproc - n_correct_int8_android_preproc
        print(f"\n  ⚠️  Le preprocessing Android dégrade la précision de {diff} classes !")
        print(f"  → Le problème vient du PREPROCESSING ANDROID (pas du modèle).")
        print(f"  → Solution : adapter TFLiteClassifier.kt pour utiliser le bon preprocessing.")
    elif n_correct_int8_correct_preproc <= n_correct_int8_android_preproc:
        print(f"\n  → Le preprocessing Android fonctionne aussi bien ou mieux.")
        print(f"  → Le problème pourrait venir du modèle INT8 lui-même.")

    if interp_float32 and n_correct_float32 > n_correct_int8_correct_preproc:
        diff2 = n_correct_float32 - n_correct_int8_correct_preproc
        print(f"\n  ⚠️  Le modèle FLOAT32 est meilleur de {diff2} classes vs INT8.")
        print(f"  → La quantification INT8 dégrade aussi la précision.")
        print(f"  → Considérer le modèle FLOAT32 pour la production.")


if __name__ == "__main__":
    main()
