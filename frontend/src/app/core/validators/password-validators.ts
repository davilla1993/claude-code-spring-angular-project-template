import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Miroir de la règle backend (shared/util/PasswordStrength) pour un retour immédiat à l'utilisateur.
 * Le backend reste l'autorité : toute évolution doit être faite des deux côtés.
 */
export const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()\-_+=[\]{};':"\\|,.<>/?]).{8,72}$/;

export const PASSWORD_RULES =
  'Entre 8 et 72 caractères, dont une majuscule, une minuscule, un chiffre et un caractère spécial.';

export function passwordStrength(): ValidatorFn {
  return (control: AbstractControl<string | null>): ValidationErrors | null => {
    const value = control.value;
    if (!value) {
      return null; // "required" gère le champ vide
    }
    return PASSWORD_PATTERN.test(value) ? null : { passwordStrength: true };
  };
}

/** Validateur de groupe : erreur `mismatch` sur le groupe si `confirmField` diffère de `field`. */
export function matchingFields(field: string, confirmField: string): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const confirm = group.get(confirmField)?.value;
    return confirm && group.get(field)?.value !== confirm ? { mismatch: true } : null;
  };
}
