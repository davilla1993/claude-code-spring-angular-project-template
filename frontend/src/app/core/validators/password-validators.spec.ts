import { FormControl, FormGroup } from '@angular/forms';
import { matchingFields, passwordStrength } from './password-validators';

describe('passwordStrength', () => {
  const validate = (value: string) => passwordStrength()(new FormControl(value));

  it.each(['Secret#123', 'Aa1!aaaa', 'A1!' + 'a'.repeat(69)])('accepts %s', (value) => {
    expect(validate(value)).toBeNull();
  });

  it.each(['Ab1!xyz', 'nouppercase#1', 'NOLOWERCASE#1', 'NoDigits#!', 'NoSpecial123', 'A1!' + 'a'.repeat(70)])(
    'rejects %s',
    (value) => {
      expect(validate(value)).toEqual({ passwordStrength: true });
    },
  );

  it('leaves empty values to the required validator', () => {
    expect(validate('')).toBeNull();
  });
});

describe('matchingFields', () => {
  it('flags the group when confirmation differs', () => {
    const group = new FormGroup(
      { password: new FormControl('Secret#123'), confirm: new FormControl('Other#123') },
      { validators: matchingFields('password', 'confirm') },
    );
    expect(group.hasError('mismatch')).toBe(true);

    group.controls.confirm.setValue('Secret#123');
    expect(group.hasError('mismatch')).toBe(false);
  });
});
