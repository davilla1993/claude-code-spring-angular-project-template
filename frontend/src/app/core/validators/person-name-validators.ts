import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Miroir de la règle backend (shared/util/PersonName) pour un retour immédiat à l'utilisateur.
 * Le backend reste l'autorité : toute évolution doit être faite des deux côtés.
 */
const MIN_LENGTH = 2;
const MAX_LENGTH = 25;

export const PERSON_NAME_RULES = `Entre ${MIN_LENGTH} et ${MAX_LENGTH} caractères.`;

export function personName(): ValidatorFn {
  return (control: AbstractControl<string | null>): ValidationErrors | null => {
    if (!control.value) {
      return null; // "required" gère le champ vide
    }
    const length = control.value.trim().length;
    return length >= MIN_LENGTH && length <= MAX_LENGTH ? null : { personName: true };
  };
}
