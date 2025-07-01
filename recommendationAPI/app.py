from flask import Flask, request, jsonify
import joblib
import pandas as pd
import json

app = Flask(__name__)

# 🔐 Set your secret API key here
API_KEY = "brandan-api-key"

# Load model and resources
model = joblib.load("xgb_exercise_model.joblib")
scaler = joblib.load("xgb_scaler.save")

with open("xgb_encoder_columns.json") as f:
    columns = json.load(f)

with open("xgb_class_to_exercise.json") as f:
    class_index_to_exercise = json.load(f)

with open("xgb_exercise_recommendations.json") as f:
    exercise_to_recommendation = json.load(f)

@app.route('/predict', methods=['POST'])
def predict():
    # 🔐 Check for API key in request headers
    api_key = request.headers.get('x-api-key')
    if api_key != API_KEY:
        return jsonify({"error": "Unauthorized. Missing or incorrect API key."}), 401

    try:
        data = request.get_json()

        # Define feature sets
        num_features = ['Age', 'Height', 'Weight', 'BMI']
        cat_features = ['Sex', 'Hypertension', 'Diabetes', 'Level', 'Fitness Goal']

        # Process numerical input
        num_data = pd.DataFrame([data])[num_features]
        num_data_scaled = pd.DataFrame(scaler.transform(num_data), columns=num_features)

        # Process categorical input
        cat_data = pd.get_dummies(pd.DataFrame([data])[cat_features])
        combined = pd.concat([num_data_scaled, cat_data], axis=1)

        # Align with training columns
        for col in columns:
            if col not in combined.columns:
                combined[col] = 0
        combined = combined[columns]

        # Predict
        prediction = model.predict(combined)[0]
        predicted_exercise = class_index_to_exercise[str(prediction)]

        # Get recommendation
        recommendation = exercise_to_recommendation.get(predicted_exercise, "No recommendation available.")

        return jsonify({
            'recommended_exercise': predicted_exercise,
            'recommendation': recommendation
        })

    except Exception as e:
        return jsonify({'error': str(e)})

if __name__ == '__main__':
    import os
    app.run(debug=True, use_reloader=False)
