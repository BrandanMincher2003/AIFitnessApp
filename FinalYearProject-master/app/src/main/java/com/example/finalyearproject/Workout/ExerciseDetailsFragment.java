package com.example.finalyearproject.Workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.finalyearproject.R;
import com.google.android.material.button.MaterialButton;

public class ExerciseDetailsFragment extends Fragment {

    private TextView name, type, targetMuscles, equipment, description;

    public ExerciseDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exercise_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        name = view.findViewById(R.id.exercise_name);
        type = view.findViewById(R.id.exercise_type);
        targetMuscles = view.findViewById(R.id.exercise_target_muscles);
        equipment = view.findViewById(R.id.exercise_equipment);
        description = view.findViewById(R.id.exercise_description);
        MaterialButton addButton = view.findViewById(R.id.button_add_exercise);

        // Retrieve passed exercise
        Exercise exercise = (Exercise) getArguments().getSerializable("exercise");

        if (exercise != null) {
            name.setText(exercise.getName());
            type.setText("Type: " + exercise.getType());
            targetMuscles.setText("Target Muscles: " + exercise.getTargetMuscles());
            equipment.setText("Equipment: " + exercise.getEquipment());
            description.setText("Description: " + exercise.getDescription());
        }




    }
}
