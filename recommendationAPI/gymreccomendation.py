import pandas as pd
import numpy as np
import json
import joblib
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.metrics import classification_report
import xgboost as xgb

# Load and clean data
df = pd.read_excel("gym recommendation/gym recommendation.xlsx")
df_model = df.drop(columns=['ID', 'Diet', 'Fitness Type'])
df_model['Level'] = df_model['Level'].replace('Obuse', 'Obese')

# Encode target
exercise_encoder = LabelEncoder()
df_model['ExercisesEncoded'] = exercise_encoder.fit_transform(df_model['Exercises'])

# Save class mapping
class_index_to_exercise = {
    int(exercise_encoder.transform([ex])[0]): ex
    for ex in df_model['Exercises'].unique()
}
with open("xgb_class_to_exercise.json", "w") as f:
    json.dump(class_index_to_exercise, f)

# Save recommendation mapping
exercise_to_recommendation = dict(zip(df['Exercises'], df['Recommendation']))
with open("xgb_exercise_recommendations.json", "w") as f:
    json.dump(exercise_to_recommendation, f)
print("✅ Recommendation mapping saved.")

# Features
numerical_cols = ['Age', 'Height', 'Weight', 'BMI']
categorical_cols = ['Sex', 'Hypertension', 'Diabetes', 'Level', 'Fitness Goal']

# Preprocessing
X_cat = pd.get_dummies(df_model[categorical_cols])
scaler = StandardScaler()
X_num = pd.DataFrame(scaler.fit_transform(df_model[numerical_cols]), columns=numerical_cols)
X = pd.concat([X_num, X_cat], axis=1)
y = df_model['ExercisesEncoded']

# Save encoder info
with open("xgb_encoder_columns.json", "w") as f:
    json.dump(X.columns.tolist(), f)
joblib.dump(scaler, "xgb_scaler.save")

# Train/test split
X_train, X_test, y_train, y_test = train_test_split(X, y, stratify=y, test_size=0.2, random_state=42)

# Train XGBoost classifier
xgb_model = xgb.XGBClassifier(
    objective='multi:softprob',
    num_class=len(np.unique(y)),
    eval_metric='mlogloss',
    use_label_encoder=False
)
xgb_model.fit(X_train, y_train)

# Evaluation
y_pred = xgb_model.predict(X_test)
print("\nClassification Report:\n", classification_report(y_test, y_pred, target_names=exercise_encoder.classes_))

# Save model
joblib.dump(xgb_model, "xgb_exercise_model.joblib")
print("✅ XGBoost model saved.")

# Print all unique options for each feature
print("\n📊 Unique values for each categorical feature:")
for col in categorical_cols:
    print(f"{col}: {df_model[col].unique().tolist()}")

print("\n📐 Summary statistics for numerical features:")
print(df_model[numerical_cols].describe())
