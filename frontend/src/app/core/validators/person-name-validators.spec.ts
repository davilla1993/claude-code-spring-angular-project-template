import { FormControl } from '@angular/forms';
import { personName } from './person-name-validators';

describe('personName', () => {
  const validate = (value: string) => personName()(new FormControl(value));

  it.each(['Li', 'Jean Paul', '  Li  ', 'a'.repeat(25)])('accepts "%s"', (value) => {
    expect(validate(value)).toBeNull();
  });

  it.each(['A', '  A  ', '     ', 'a'.repeat(26)])('rejects "%s"', (value) => {
    expect(validate(value)).toEqual({ personName: true });
  });

  it('leaves empty values to the required validator', () => {
    expect(validate('')).toBeNull();
  });
});
