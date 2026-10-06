import { Pipe, PipeTransform } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { PASSWORD_RULES } from '../../core/validators/password-validators';

/**
 * Message d'erreur d'un champ de formulaire, uniquement une fois le champ touché.
 * Impur : réévalué à chaque détection de changement, pour suivre l'état du contrôle.
 *
 * Usage : `@if (form.controls.email | fieldError; as error) { <p class="field-error" ...>{{ error }}</p> }`
 */
@Pipe({ name: 'fieldError', pure: false })
export class FieldErrorPipe implements PipeTransform {
  transform(control: AbstractControl | null | undefined): string | null {
    if (!control || !control.errors || !control.touched) {
      return null;
    }
    const errors = control.errors;
    if (errors['required']) return 'Ce champ est obligatoire.';
    if (errors['email']) return 'Adresse email invalide.';
    if (errors['passwordStrength']) return PASSWORD_RULES;
    if (errors['maxlength']) return `${errors['maxlength'].requiredLength} caractères maximum.`;
    if (errors['minlength']) return `${errors['minlength'].requiredLength} caractères minimum.`;
    if (errors['pattern']) return 'Format invalide.';
    return 'Valeur invalide.';
  }
}
