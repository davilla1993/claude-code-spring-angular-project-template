import { Pipe, PipeTransform } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { FieldErrorPipe } from './field-error-pipe';

const MISMATCH = 'Les mots de passe ne correspondent pas.';

/**
 * Erreur d'un champ de confirmation : erreurs propres au champ, puis erreur `mismatch` du groupe
 * (voir validateur matchingFields).
 *
 * Usage : `form | confirmFieldError: 'confirmPassword'`
 */
@Pipe({ name: 'confirmFieldError', pure: false })
export class ConfirmFieldErrorPipe implements PipeTransform {
  private readonly fieldError = new FieldErrorPipe();

  transform(group: AbstractControl, confirmField: string): string | null {
    const confirm = group.get(confirmField);
    const ownError = this.fieldError.transform(confirm);
    if (ownError) {
      return ownError;
    }
    return confirm?.touched && group.hasError('mismatch') ? MISMATCH : null;
  }
}
