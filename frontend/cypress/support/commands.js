/* global Cypress, cy */
Cypress.Commands.add("loginAs", (identifier, password) => {
  cy.visit("/login");
  cy.findByLabelText(/email or staff id/i).type(identifier);
  cy.findByLabelText(/password/i).type(password);
  cy.findByRole("button", { name: /log in/i }).click();
});
